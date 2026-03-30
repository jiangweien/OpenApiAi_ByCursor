package com.dsg.aiSdk.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从助手合并文本中拆分「思考」与「回复」。不同模型格式不一，此处覆盖常见标记。
 */
public final class AssistantContentParser {

    private static final Pattern THINK_BLOCK =
            Pattern.compile("(?is)```(?:think|thinking)?\\s*([\\s\\S]*?)```");
    private static final Pattern TAG_THINK =
            Pattern.compile("(?is)<think(?:ing)?>([\\s\\S]*?)</think(?:ing)?>");

    private AssistantContentParser() {}

    public static String[] splitThinkingAndReply(String combined) {
        if (combined == null || combined.trim().isEmpty()) {
            return new String[] {"", ""};
        }
        String work = combined;
        StringBuilder thinking = new StringBuilder();

        Matcher m = TAG_THINK.matcher(work);
        while (m.find()) {
            if (thinking.length() > 0) {
                thinking.append("\n\n");
            }
            thinking.append(m.group(1).trim());
        }
        work = TAG_THINK.matcher(work).replaceAll("").trim();

        m = THINK_BLOCK.matcher(work);
        while (m.find()) {
            if (thinking.length() > 0) {
                thinking.append("\n\n");
            }
            thinking.append(m.group(1).trim());
        }
        work = THINK_BLOCK.matcher(work).replaceAll("").trim();

        String t = thinking.toString().trim();
        String reply = work.isEmpty() ? combined.trim() : work;
        if (t.isEmpty()) {
            return new String[] {"", reply};
        }
        return new String[] {t, reply};
    }
}
