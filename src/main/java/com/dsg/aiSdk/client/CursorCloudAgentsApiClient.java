package com.dsg.aiSdk.client;

import com.dsg.aiSdk.CursorApiException;
import com.dsg.aiSdk.config.CursorCredentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cursor Cloud Agents HTTP 客户端（官方文档：curl -u API_KEY:）。
 * 使用 {@link HttpURLConnection}，兼容 Java 8（与 Maven/Surefire 使用 JDK8 运行测试一致）。
 */
public final class CursorCloudAgentsApiClient implements CursorAgentsApi {

    private static final int TIMEOUT_MS = 120_000;

    private final CursorCredentials credentials;

    public CursorCloudAgentsApiClient(CursorCredentials credentials) {
        this.credentials = credentials;
    }

    public JsonObject getMe() {
        return getJson("/v0/me");
    }

    @Override
    public JsonObject createAgent(JsonObject body) {
        return postJson("/v0/agents", body);
    }

    @Override
    public JsonObject addFollowup(String agentId, JsonObject body) {
        return postJson("/v0/agents/" + urlEncode(agentId) + "/followup", body);
    }

    @Override
    public JsonObject getAgent(String agentId) {
        return getJson("/v0/agents/" + urlEncode(agentId));
    }

    @Override
    public JsonObject getConversation(String agentId) {
        return getJson("/v0/agents/" + urlEncode(agentId) + "/conversation");
    }

    private static String urlEncode(String id) {
        try {
            return URLEncoder.encode(id, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8", e);
        }
    }

    private String authHeader() {
        String raw = credentials.apiKey() + ":";
        String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + b64;
    }

    private JsonObject getJson(String path) {
        try {
            return sendHttp("GET", credentials.baseUrl() + path, null);
        } catch (IOException e) {
            throw new CursorApiException("请求失败: " + e.getMessage(), -1, "");
        }
    }

    private JsonObject postJson(String path, JsonObject body) {
        try {
            return sendHttp("POST", credentials.baseUrl() + path, body.toString());
        } catch (IOException e) {
            throw new CursorApiException("请求失败: " + e.getMessage(), -1, "");
        }
    }

    private JsonObject sendHttp(String method, String urlStr, String jsonBody) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Authorization", authHeader());
        conn.setRequestProperty("Accept", "application/json");
        if ("POST".equals(method)) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            OutputStream os = conn.getOutputStream();
            try {
                os.write(bytes);
            } finally {
                os.close();
            }
        }
        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String body = readStream(stream);
        conn.disconnect();
        if (code >= 200 && code < 300) {
            if (body.trim().isEmpty()) {
                return new JsonObject();
            }
            JsonElement el = new JsonParser().parse(body);
            return el.isJsonObject() ? el.getAsJsonObject() : unexpected(body, code);
        }
        throw new CursorApiException("Cursor API 错误 HTTP " + code, code, body);
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            int n;
            while ((n = stream.read(b)) != -1) {
                buf.write(b, 0, n);
            }
            return new String(buf.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            stream.close();
        }
    }

    private static JsonObject unexpected(String body, int code) {
        throw new CursorApiException("非 JSON 响应 HTTP " + code, code, body);
    }

    public static JsonArray messagesArray(JsonObject conversationResponse) {
        if (!conversationResponse.has("messages") || !conversationResponse.get("messages").isJsonArray()) {
            return new JsonArray();
        }
        return conversationResponse.getAsJsonArray("messages");
    }
}
