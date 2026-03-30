package com.dsg.aiSdk.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CursorCredentialsTest {

    @Test
    void trimsKeyAndStripsTrailingSlashFromBaseUrl() {
        CursorCredentials c = new CursorCredentials("  key123  ", "https://api.cursor.com/");
        assertEquals("key123", c.apiKey());
        assertEquals("https://api.cursor.com", c.baseUrl());
    }

    @Test
    void rejectsNullOrEmptyApiKey() {
        assertThrows(NullPointerException.class, () -> new CursorCredentials(null));
        assertThrows(IllegalArgumentException.class, () -> new CursorCredentials("   "));
    }

    @Test
    void rejectsNullBaseUrl() {
        assertThrows(NullPointerException.class, () -> new CursorCredentials("k", null));
    }
}
