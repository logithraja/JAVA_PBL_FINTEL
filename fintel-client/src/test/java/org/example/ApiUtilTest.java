package org.example;

import org.example.utils.ApiUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiUtilTest {

    @AfterEach
    void tearDown() {
        ApiUtil.clearAuthToken();
    }

    @Test
    void testAuthTokenManagement() {
        assertNull(ApiUtil.getAuthToken());

        ApiUtil.setAuthToken("sample-jwt-token-12345");
        assertEquals("sample-jwt-token-12345", ApiUtil.getAuthToken());

        ApiUtil.clearAuthToken();
        assertNull(ApiUtil.getAuthToken());
    }
}
