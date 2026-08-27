package org.example.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvConfig {
    private static final Map<String, String> ENV_VARS = new HashMap<>();

    static {
        loadEnvFile();
    }

    public static void loadEnvFile() {
        File envFile = new File(".env");
        if (!envFile.exists()) {
            envFile = new File("../.env");
        }

        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        String key = line.substring(0, eqIdx).trim();
                        String value = line.substring(eqIdx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        ENV_VARS.put(key, value);
                    }
                }
            } catch (IOException e) {
                System.err.println("EnvConfig: Could not read .env file: " + e.getMessage());
            }
        }
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String envVal = System.getenv(key);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }

        String propVal = System.getProperty(key);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return propVal.trim();
        }

        String fileVal = ENV_VARS.get(key);
        if (fileVal != null && !fileVal.trim().isEmpty()) {
            return fileVal.trim();
        }

        return defaultValue;
    }

    public static String getOpenAiKey() {
        return get("OPENAI_API_KEY", get("OPENAI_KEY", ""));
    }

    public static String getMistralKey() {
        return get("MISTRAL_API_KEY", get("MISTRAL_KEY", ""));
    }

    public static String getGeminiKey() {
        return get("GEMINI_API_KEY", get("GEMINI_KEY", ""));
    }

    public static String getApiBaseUrl() {
        return get("FINTEL_API_URL", "http://localhost:8080");
    }
}
