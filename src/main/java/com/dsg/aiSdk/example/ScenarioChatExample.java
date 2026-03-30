package com.dsg.aiSdk.example;

import com.dsg.aiSdk.config.CursorCredentials;
import com.dsg.aiSdk.model.ChatTurnResult;
import com.dsg.aiSdk.model.ConversationScenario;
import com.dsg.aiSdk.service.ScenarioConversationSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 控制台多轮对话示例。
 * <ul>
 *   <li>{@code CURSOR_API_KEY}、{@code CURSOR_REPO} 必填；{@code CURSOR_REF} 可选（默认 main）</li>
 *   <li>{@code CURSOR_SCENARIO} 可选：场景说明；未设置时使用内置默认文案</li>
 *   <li>{@code CURSOR_MODEL} 可选：模型 ID（默认 {@code claude-4.5-sonnet-thinking}）</li>
 * </ul>
 * 交互命令：输入 {@code exit} / {@code quit} 退出；{@code :reset} 丢弃当前 Agent 下次重新创建。
 */
public final class ScenarioChatExample {

    private static final String DEFAULT_SCENARIO =
            "你是有帮助的助手，在对话中遵守用户给定的场景约束，回答清晰；若使用思考标记请单独包裹便于展示。";

    private ScenarioChatExample() {}

    public static void main(String[] args) throws Exception {
        // String key = System.getenv("CURSOR_API_KEY");
        String key = "crsr_2dbcbfa925ac549fd6f36eaa7d4e693913cb5333f02c61dd3ca332e1aab742f4";
        String repo = System.getenv("CURSOR_REPO");
        if (key == null || key.trim().isEmpty() || repo == null || repo.trim().isEmpty()) {
            System.err.println("请设置环境变量: CURSOR_API_KEY, CURSOR_REPO（例如 https://github.com/org/repo）");
            System.exit(1);
        }
        String ref = System.getenv("CURSOR_REF");
        if (ref == null || ref.trim().isEmpty()) {
            ref = "main";
        }
        String scenarioText = System.getenv("CURSOR_SCENARIO");
        if (scenarioText == null || scenarioText.trim().isEmpty()) {
            scenarioText = DEFAULT_SCENARIO;
        }
        String model = System.getenv("CURSOR_MODEL");
        if (model == null || model.trim().isEmpty()) {
            model = "claude-4.5-sonnet-thinking";
        }

        CursorCredentials cred = new CursorCredentials(key);
        ScenarioConversationSession session = new ScenarioConversationSession(cred);
        ConversationScenario scenario =
                new ConversationScenario(scenarioText, repo, ref, model, false);
        session.setScenario(scenario);

        System.out.println("已连接 Cursor Cloud Agent（仓库: " + repo + ", 分支: " + ref + "）");
        System.out.println("输入内容后回车发送；exit / quit 退出；:reset 重置会话 Agent。");
        System.out.println();

        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        runLoop(session, in);
    }

    private static void runLoop(ScenarioConversationSession session, BufferedReader in)
            throws IOException, InterruptedException {
        while (true) {
            System.out.print("> ");
            System.out.flush();
            String line = in.readLine();
            if (line == null) {
                break;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if ("exit".equalsIgnoreCase(trimmed) || "quit".equalsIgnoreCase(trimmed)) {
                System.out.println("再见。");
                break;
            }
            if (":reset".equals(trimmed)) {
                session.reset();
                System.out.println("[已 reset，下一轮将创建新 Agent]");
                continue;
            }

            try {
                ChatTurnResult r = session.chat(trimmed);
                if (!r.thinkingText().isEmpty()) {
                    System.out.println("--- 思考 ---");
                    System.out.println(r.thinkingText());
                    System.out.println("--- 回复 ---");
                }
                System.out.println(r.replyText());
            } catch (RuntimeException ex) {
                System.err.println("[错误] " + ex.getMessage());
            }
            System.out.println();
        }
    }
}
