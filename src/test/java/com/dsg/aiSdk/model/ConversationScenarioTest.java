package com.dsg.aiSdk.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationScenarioTest {

    @Test
    void ofUsesThinkingModelAndMainRef() {
        ConversationScenario s = ConversationScenario.of("场景", "https://github.com/a/b", null);
        assertEquals("场景", s.scenarioText());
        assertEquals("https://github.com/a/b", s.repositoryUrl());
        assertEquals("main", s.gitRef());
        assertEquals("claude-4.5-sonnet-thinking", s.modelId());
        assertFalse(s.autoCreatePr());
    }

    @Test
    void rejectsEmptyScenarioOrRepo() {
        assertThrows(IllegalArgumentException.class, () -> new ConversationScenario("", "https://x", "main", null, false));
        assertThrows(IllegalArgumentException.class, () -> new ConversationScenario("ok", "", "main", null, false));
    }
}
