package org.example.utils;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ApiUtil {
    private static String authToken = null;

    public enum RequestMethod { POST, GET, PUT, DELETE }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void clearAuthToken() {
        authToken = null;
    }

    public static HttpURLConnection fetchApi(String apiPath, RequestMethod requestMethod, JsonObject jsonData) {
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

            return conn;
        } catch (IOException e) {
            System.err.println("ApiUtil HTTP Error [" + requestMethod + " " + apiPath + "]: " + e.getMessage());
        }

        return null;
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
