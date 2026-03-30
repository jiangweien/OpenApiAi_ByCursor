package com.dsg.aiSdk.service;

import com.dsg.aiSdk.client.CursorAgentsApi;
import com.dsg.aiSdk.model.ChatTurnResult;
import com.dsg.aiSdk.model.ConversationScenario;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScenarioConversationSessionTest {

    private static final String REPO = "https://github.com/example/repo";

    private CursorAgentsApi api;
    private ScenarioConversationSession session;

    @BeforeEach
    void setUp() {
        api = Mockito.mock(CursorAgentsApi.class);
        session = new ScenarioConversationSession(
                api,
                Duration.ofMillis(5),
                Duration.ofSeconds(2));
        ConversationScenario scenario = new ConversationScenario(
                "测试场景：简短回答。",
                REPO,
                "main",
                null,
                false);
        session.setScenario(scenario);
    }

    @Test
    void chatFailsWhenScenarioNotSet() {
        ScenarioConversationSession s = new ScenarioConversationSession(api, Duration.ofMillis(1), Duration.ofMillis(200));
        assertThrows(IllegalStateException.class, () -> s.chat("hi"));
    }

    @Test
    void chatFailsOnBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> session.chat("  "));
    }

    @Test
    void firstChatCreatesAgentAndReturnsAssistantSlice() throws Exception {
        when(api.createAgent(any(JsonObject.class)))
                .thenReturn(new JsonParser().parse("{\"id\":\"bc_test\"}").getAsJsonObject());
        when(api.getConversation(eq("bc_test")))
                .thenReturn(conversation("bc_test", msg("u1", "user_message", "prompt"), msg("a1", "assistant_message", "助手回复")));

        ChatTurnResult r = session.chat("你好");

        assertEquals("bc_test", session.getAgentId());
        assertEquals("助手回复", r.replyText());
        assertEquals("助手回复", r.combinedAssistantText());
        verify(api, atLeastOnce()).createAgent(any(JsonObject.class));
        verify(api, atLeastOnce()).getConversation(eq("bc_test"));
    }

    @Test
    void secondChatUsesFollowup() throws Exception {
        JsonObject conv1 = conversation(
                "bc_x",
                msg("u1", "user_message", "第一轮"),
                msg("a1", "assistant_message", "答1"));
        JsonObject conv2 = conversation(
                "bc_x",
                msg("u1", "user_message", "第一轮"),
                msg("a1", "assistant_message", "答1"),
                msg("u2", "user_message", "第二轮"),
                msg("a2", "assistant_message", "答2"));

        when(api.createAgent(any(JsonObject.class)))
                .thenReturn(new JsonParser().parse("{\"id\":\"bc_x\"}").getAsJsonObject());

        AtomicInteger convCalls = new AtomicInteger();
        when(api.getConversation(eq("bc_x"))).thenAnswer((Answer<JsonObject>) inv -> {
            int n = convCalls.incrementAndGet();
            /* 首轮轮询 + 第二轮 baseline + 若干次仅含首条 user 的 conv1，最后一次再返回含第二条 user 的 conv2 */
            if (n <= 4) {
                return conv1;
            }
            return conv2;
        });
        when(api.getAgent(eq("bc_x")))
                .thenReturn(new JsonParser().parse("{\"status\":\"RUNNING\"}").getAsJsonObject());
        when(api.addFollowup(eq("bc_x"), any(JsonObject.class)))
                .thenReturn(new JsonParser().parse("{\"id\":\"bc_x\"}").getAsJsonObject());

        session.chat("第一轮");
        ChatTurnResult r2 = session.chat("第二轮");

        assertEquals("答2", r2.replyText());
        verify(api).addFollowup(eq("bc_x"), any(JsonObject.class));
    }

    @Test
    void thinkingTagsSplitIntoThinkingAndReply() throws Exception {
        when(api.createAgent(any(JsonObject.class)))
                .thenReturn(new JsonParser().parse("{\"id\":\"bc_t\"}").getAsJsonObject());
        String assistantText = "\u003Cthink\u003E内部推理\u003C/think\u003E\n\n对外说明。";
        when(api.getConversation(eq("bc_t")))
                .thenReturn(conversation(
                        "bc_t",
                        msg("u1", "user_message", "x"),
                        msg("a1", "assistant_message", assistantText)));

        ChatTurnResult r = session.chat("问");

        assertEquals("内部推理", r.thinkingText());
        assertTrue(r.replyText().contains("对外说明"));
    }

    @Test
    void agentErrorStatusThrows() throws Exception {
        when(api.createAgent(any(JsonObject.class)))
                .thenReturn(new JsonParser().parse("{\"id\":\"bc_e\"}").getAsJsonObject());
        when(api.getConversation(eq("bc_e")))
                .thenReturn(conversation("bc_e", msg("u1", "user_message", "x")));
        when(api.getAgent(eq("bc_e")))
                .thenReturn(new JsonParser().parse("{\"status\":\"ERROR\"}").getAsJsonObject());

        assertThrows(IllegalStateException.class, () -> session.chat("x"));
    }

    private static JsonObject msg(String id, String type, String text) {
        JsonObject m = new JsonObject();
        m.addProperty("id", id);
        m.addProperty("type", type);
        m.addProperty("text", text);
        return m;
    }

    private static JsonObject conversation(String agentId, JsonObject... messages) {
        JsonObject root = new JsonObject();
        root.addProperty("id", agentId);
        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (JsonObject o : messages) {
            arr.add(o);
        }
        root.add("messages", arr);
        return root;
    }
}
