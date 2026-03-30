package com.dsg.aiSdk.model;

import java.util.Objects;

/**
 * 对话场景：将「场景说明」写入每次与 Cloud Agent 交互的 prompt，约束模型行为。
 * <p>
 * Cloud Agents 必须绑定 GitHub 仓库；请使用你有权访问的仓库 URL 与分支。
 */
public final class ConversationScenario {

    private final String scenarioText;
    private final String repositoryUrl;
    private final String gitRef;
    /** 建议使用带 thinking 的模型 ID，例如 {@code claude-4.5-sonnet-thinking}；{@code null} 表示由服务端选择默认模型 */
    private final String modelId;
    private final boolean autoCreatePr;

    public ConversationScenario(
            String scenarioText,
            String repositoryUrl,
            String gitRef,
            String modelId,
            boolean autoCreatePr) {
        this.scenarioText = Objects.requireNonNull(scenarioText, "scenarioText").trim();
        this.repositoryUrl = Objects.requireNonNull(repositoryUrl, "repositoryUrl").trim();
        this.gitRef = (gitRef == null || gitRef.trim().isEmpty()) ? "main" : gitRef.trim();
        this.modelId = (modelId == null || modelId.trim().isEmpty()) ? null : modelId.trim();
        this.autoCreatePr = autoCreatePr;
        if (this.scenarioText.isEmpty()) {
            throw new IllegalArgumentException("scenarioText is empty");
        }
        if (this.repositoryUrl.isEmpty()) {
            throw new IllegalArgumentException("repositoryUrl is empty");
        }
    }

    public static ConversationScenario of(String scenarioText, String repositoryUrl, String gitRef) {
        return new ConversationScenario(scenarioText, repositoryUrl, gitRef, "claude-4.5-sonnet-thinking", false);
    }

    public String scenarioText() {
        return scenarioText;
    }

    public String repositoryUrl() {
        return repositoryUrl;
    }

    public String gitRef() {
        return gitRef;
    }

    public String modelId() {
        return modelId;
    }

    public boolean autoCreatePr() {
        return autoCreatePr;
    }
}
