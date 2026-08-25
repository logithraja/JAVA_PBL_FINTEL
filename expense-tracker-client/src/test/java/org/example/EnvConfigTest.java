package org.example;

import org.example.utils.EnvConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnvConfigTest {

    @Test
    void testGetDefaultValueWhenKeyNotFound() {
        String val = EnvConfig.get("NON_EXISTENT_FINVORA_KEY_XYZ", "default123");
        assertEquals("default123", val);
    }

    @Test
    void testGetApiBaseUrl() {
        String baseUrl = EnvConfig.getApiBaseUrl();
        assertNotNull(baseUrl);
        assertTrue(baseUrl.startsWith("http"));
    }

    @Test
    void testGetKeysDoNotNullPointer() {
        assertNotNull(EnvConfig.getOpenAiKey());
        assertNotNull(EnvConfig.getMistralKey());
        assertNotNull(EnvConfig.getGeminiKey());
    }
}
