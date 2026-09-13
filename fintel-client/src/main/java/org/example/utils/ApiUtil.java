package org.example.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ApiUtil {
    private static String authToken = null;
    private static String refreshToken = null;

    public enum RequestMethod { POST, GET, PUT, DELETE }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setRefreshToken(String token) {
        refreshToken = token;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static void clearTokens() {
        authToken = null;
        refreshToken = null;
    }

    public static void clearAuthToken() {
        clearTokens();
    }

    public static HttpURLConnection fetchApi(String apiPath, RequestMethod requestMethod, JsonObject jsonData) {
        return fetchApiInternal(apiPath, requestMethod, jsonData, false);
    }

    private static HttpURLConnection fetchApiInternal(String apiPath, RequestMethod requestMethod, JsonObject jsonData, boolean isRetry) {
        try {
            String baseUrl = EnvConfig.getApiBaseUrl();
            URL url = new URL(baseUrl + apiPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod(requestMethod.toString());
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            if (authToken != null && !authToken.trim().isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken.trim());
            }

            conn.setRequestProperty("Accept", "application/json");

            if (jsonData != null && requestMethod != RequestMethod.GET) {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonData.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            // Transparent token refresh on 401 Unauthorized
            if (!isRetry && !apiPath.startsWith("/api/v1/user/login") && !apiPath.startsWith("/api/v1/user/refresh")) {
                int responseCode = conn.getResponseCode();
                if (responseCode == 401 && refreshToken != null && !refreshToken.trim().isEmpty()) {
                    conn.disconnect();
                    boolean refreshed = attemptTokenRefresh();
                    if (refreshed) {
                        // Retry request once with the new access token
                        return fetchApiInternal(apiPath, requestMethod, jsonData, true);
                    }
                }
            }

            return conn;
        } catch (IOException e) {
            System.err.println("ApiUtil HTTP Error [" + requestMethod + " " + apiPath + "]: " + e.getMessage());
        }

        return null;
    }

    private static synchronized boolean attemptTokenRefresh() {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return false;
        }

        try {
            String baseUrl = EnvConfig.getApiBaseUrl();
            URL url = new URL(baseUrl + "/api/v1/user/refresh");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            JsonObject payload = new JsonObject();
            payload.addProperty("refreshToken", refreshToken);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                String responseBody = readApiResponse(conn);
                if (responseBody != null) {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (json.has("token") && !json.get("token").isJsonNull()) {
                        authToken = json.get("token").getAsString();
                    }
                    if (json.has("refreshToken") && !json.get("refreshToken").isJsonNull()) {
                        refreshToken = json.get("refreshToken").getAsString();
                    }
                    conn.disconnect();
                    return true;
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("ApiUtil: Token refresh request failed: " + e.getMessage());
        }

        // If refresh failed (token expired/revoked), clear stored credentials
        clearTokens();
        return false;
    }

    public static String readApiResponse(HttpURLConnection conn) {
        if (conn == null) return null;

        try {
            InputStream is = (conn.getResponseCode() >= 400) ? conn.getErrorStream() : conn.getInputStream();
            if (is == null) return null;

            StringBuilder resultJson = new StringBuilder();
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    resultJson.append(scanner.nextLine());
                }
            }

            return resultJson.toString();
        } catch (IOException e) {
            System.err.println("ApiUtil Error reading response: " + e.getMessage());
        }

        return null;
    }
}
