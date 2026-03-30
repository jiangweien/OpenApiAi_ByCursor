package com.dsg.aiSdk.model;

import java.util.Objects;

public final class ConversationMessage {

    private final String id;
    private final String type;
    private final String text;

    public ConversationMessage(String id, String type, String text) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.text = text == null ? "" : text;
    }

    public String id() {
        return id;
    }

    public String type() {
        return type;
    }

    public String text() {
        return text;
    }

    public boolean isUser() {
        return "user_message".equals(type);
    }

    public boolean isAssistant() {
        return "assistant_message".equals(type);
    }
}
