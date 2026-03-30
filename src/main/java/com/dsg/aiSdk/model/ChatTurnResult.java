package com.dsg.aiSdk.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 单轮交互结果：从助手侧消息中尽量拆分「思考过程」与「最终回复」。
 */
public final class ChatTurnResult {

    private final String thinkingText;
    private final String replyText;
    private final String combinedAssistantText;
    private final List<ConversationMessage> assistantMessagesSinceTurn;

    public ChatTurnResult(
            String thinkingText,
            String replyText,
            String combinedAssistantText,
            List<ConversationMessage> assistantMessagesSinceTurn) {
        this.thinkingText = thinkingText == null ? "" : thinkingText;
        this.replyText = replyText == null ? "" : replyText;
        this.combinedAssistantText = combinedAssistantText == null ? "" : combinedAssistantText;
        this.assistantMessagesSinceTurn = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(assistantMessagesSinceTurn, "assistantMessagesSinceTurn")));
    }

    public String thinkingText() {
        return thinkingText;
    }

    public String replyText() {
        return replyText;
    }

    /** 本轮助手侧原始拼接文本（用于排查或未拆分场景） */
    public String combinedAssistantText() {
        return combinedAssistantText;
    }

    public List<ConversationMessage> assistantMessagesSinceTurn() {
        return assistantMessagesSinceTurn;
    }
}
