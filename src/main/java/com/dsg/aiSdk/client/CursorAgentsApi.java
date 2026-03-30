package com.dsg.aiSdk.client;

import com.google.gson.JsonObject;

/**
 * Cursor Cloud Agents 调用抽象，便于单元测试替换实现。
 */
public interface CursorAgentsApi {

    JsonObject createAgent(JsonObject body);

    JsonObject addFollowup(String agentId, JsonObject body);

    JsonObject getAgent(String agentId);

    JsonObject getConversation(String agentId);
}
