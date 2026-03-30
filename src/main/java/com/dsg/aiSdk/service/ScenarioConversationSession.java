package com.dsg.aiSdk.service;

import com.dsg.aiSdk.internal.AssistantContentParser;
import com.dsg.aiSdk.client.CursorAgentsApi;
import com.dsg.aiSdk.client.CursorCloudAgentsApiClient;
import com.dsg.aiSdk.config.CursorCredentials;
import com.dsg.aiSdk.model.ChatTurnResult;
import com.dsg.aiSdk.model.ConversationMessage;
import com.dsg.aiSdk.model.ConversationScenario;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 在已设定的「对话场景」下，通过 Cursor Cloud Agent 完成多轮输入，并拉取助手回复；
 * 对合并后的助手文本尝试拆分「思考过程」与「正式回答」。
 * <p>
 * 说明：Cloud Agent 面向仓库任务，需有效 GitHub 仓库；纯聊天也会占用 Agent 配额。
 */
public final class ScenarioConversationSession {

    private static final String PROMPT_TEMPLATE =
            "【对话场景设定 — 请始终遵守】\n"
                    + "%s\n\n"
                    + "【本轮用户输入】\n"
                    + "%s\n";

    private final CursorAgentsApi api;
    private ConversationScenario scenario;
    private String agentId;

    private final Duration pollInterval;
    private final Duration maxWait;

    public ScenarioConversationSession(CursorCredentials credentials) {
        this(new CursorCloudAgentsApiClient(credentials), Duration.ofSeconds(2), Duration.ofMinutes(15));
    }

    public ScenarioConversationSession(
            CursorAgentsApi api,
            Duration pollInterval,
            Duration maxWait) {
        this.api = api;
        this.pollInterval = pollInterval;
        this.maxWait = maxWait;
    }

    /** 1）对话场景设置：更新场景说明（若已有 agent 仍在使用旧场景，请 {@link #reset()} 后重开） */
    public void setScenario(ConversationScenario scenario) {
        this.scenario = scenario;
    }

    public ConversationScenario getScenario() {
        return scenario;
    }

    /** 当前会话绑定的 Agent ID（首轮对话成功后可用） */
    public String getAgentId() {
        return agentId;
    }

    /** 丢弃当前 Agent，下次 {@link #chat(String)} 将重新创建 */
    public void reset() {
        this.agentId = null;
    }

    /**
     * 2）在场景范围内交互：首轮会创建 Cloud Agent，后续走 follow-up。
     */
    public ChatTurnResult chat(String userInput) throws InterruptedException {
        if (scenario == null) {
            throw new IllegalStateException("请先调用 setScenario(ConversationScenario)");
        }
        String trimmed = userInput == null ? "" : userInput.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("userInput 为空");
        }

        String prompt = String.format(PROMPT_TEMPLATE, scenario.scenarioText(), trimmed);
        int baselineUserCount = 0;
        if (agentId == null) {
            JsonObject body = buildLaunchBody(prompt);
            JsonObject created = api.createAgent(body);
            agentId = created.get("id").getAsString();
        } else {
            JsonObject conv = api.getConversation(agentId);
            baselineUserCount = countUserMessages(CursorCloudAgentsApiClient.messagesArray(conv));
            JsonObject follow = new JsonObject();
            JsonObject p = new JsonObject();
            p.addProperty("text", prompt);
            follow.add("prompt", p);
            api.addFollowup(agentId, follow);
        }

        waitForAssistantAfterUserOrdinal(baselineUserCount + 1);
        return buildTurnResult(baselineUserCount);
    }

    private JsonObject buildLaunchBody(String promptText) {
        JsonObject root = new JsonObject();
        JsonObject prompt = new JsonObject();
        prompt.addProperty("text", promptText);
        root.add("prompt", prompt);
        if (scenario.modelId() != null) {
            root.addProperty("model", scenario.modelId());
        }
        JsonObject source = new JsonObject();
        source.addProperty("repository", scenario.repositoryUrl());
        source.addProperty("ref", scenario.gitRef());
        root.add("source", source);
        JsonObject target = new JsonObject();
        target.addProperty("autoCreatePr", scenario.autoCreatePr());
        root.add("target", target);
        return root;
    }

    private static int countUserMessages(JsonArray messages) {
        int n = 0;
        for (int i = 0; i < messages.size(); i++) {
            JsonObject m = messages.get(i).getAsJsonObject();
            if ("user_message".equals(jsonString(m, "type"))) {
                n++;
            }
        }
        return n;
    }

    /** 等待「第 ordinal 条 user_message」之后出现至少一条 assistant_message（且在该 user 与下一条 user 之间） */
    private void waitForAssistantAfterUserOrdinal(int ordinal) throws InterruptedException {
        long deadline = System.nanoTime() + maxWait.toNanos();
        while (System.nanoTime() < deadline) {
            JsonObject conv = api.getConversation(agentId);
            JsonArray arr = CursorCloudAgentsApiClient.messagesArray(conv);
            if (hasAssistantAfterNthUser(arr, ordinal)) {
                return;
            }
            JsonObject status = api.getAgent(agentId);
            String st = jsonString(status, "status");
            if ("ERROR".equals(st) || "EXPIRED".equals(st)) {
                throw new IllegalStateException("Agent 状态异常: " + st);
            }
            Thread.sleep(pollInterval.toMillis());
        }
        throw new IllegalStateException("等待助手回复超时（" + (maxWait.toMillis() / 1000) + "s）");
    }

    private static boolean hasAssistantAfterNthUser(JsonArray messages, int n) {
        if (n < 1) {
            return false;
        }
        int idx = findNthUserIndex(messages, n);
        if (idx < 0) {
            return false;
        }
        for (int i = idx + 1; i < messages.size(); i++) {
            JsonObject m = messages.get(i).getAsJsonObject();
            String type = jsonString(m, "type");
            if ("user_message".equals(type)) {
                return false;
            }
            if ("assistant_message".equals(type)) {
                return true;
            }
        }
        return false;
    }

    private ChatTurnResult buildTurnResult(int baselineUserCountBeforeTurn) {
        JsonObject conv = api.getConversation(agentId);
        JsonArray arr = CursorCloudAgentsApiClient.messagesArray(conv);
        int targetUserIndex = findNthUserIndex(arr, baselineUserCountBeforeTurn + 1);
        if (targetUserIndex < 0) {
            return new ChatTurnResult("", "", "", Collections.<ConversationMessage>emptyList());
        }
        List<ConversationMessage> slice = new ArrayList<>();
        for (int i = targetUserIndex + 1; i < arr.size(); i++) {
            JsonObject m = arr.get(i).getAsJsonObject();
            String type = jsonString(m, "type");
            if ("user_message".equals(type)) {
                break;
            }
            if ("assistant_message".equals(type)) {
                slice.add(new ConversationMessage(
                        jsonString(m, "id"),
                        type,
                        jsonString(m, "text")));
            }
        }
        String combined = slice.stream().map(ConversationMessage::text).reduce((a, b) -> a + "\n\n" + b).orElse("");
        String[] parts = AssistantContentParser.splitThinkingAndReply(combined);
        String thinking = parts[0];
        String reply = parts[1];
        if (thinking.isEmpty() && !combined.isEmpty()) {
            reply = combined;
        }
        return new ChatTurnResult(thinking, reply, combined, slice);
    }

    private static int findNthUserIndex(JsonArray messages, int n) {
        int c = 0;
        for (int i = 0; i < messages.size(); i++) {
            JsonObject m = messages.get(i).getAsJsonObject();
            if ("user_message".equals(jsonString(m, "type"))) {
                c++;
                if (c == n) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String jsonString(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            return "";
        }
        return o.get(key).getAsString();
    }
}
