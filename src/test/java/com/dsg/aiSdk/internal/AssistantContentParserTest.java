package com.dsg.aiSdk.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssistantContentParserTest {

    @Test
    void extractsThinkTagBlock() {
        String input = "\u003Cthink\u003E步骤一\n步骤二\u003C/think\u003E\n\n最终回答一句。";
        String[] p = AssistantContentParser.splitThinkingAndReply(input);
        assertEquals("步骤一\n步骤二", p[0]);
        assertEquals("最终回答一句。", p[1]);
    }

    @Test
    void extractsTripleBacktickThinkFence() {
        String[] p = AssistantContentParser.splitThinkingAndReply(
                "```think\n推理A\n```\n\n给用户：你好。");
        assertTrue(p[0].contains("推理A"));
        assertTrue(p[1].contains("给用户"));
    }

    @Test
    void noMarkersPutsAllInReply() {
        String[] p = AssistantContentParser.splitThinkingAndReply("只有正文没有标记。");
        assertEquals("", p[0]);
        assertEquals("只有正文没有标记。", p[1]);
    }

    @Test
    void blankReturnsEmpty() {
        String[] p = AssistantContentParser.splitThinkingAndReply("   ");
        assertEquals("", p[0]);
        assertEquals("", p[1]);
    }
}
