package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.utils.EnvConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AIEngine {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    public static String generateText(String systemPrompt, String userMessage) {
        String enhancedSystemPrompt = systemPrompt + "\n\nIMPORTANT: DO NOT USE ANY MARKDOWN FORMATTING. DO NOT USE ASTERISKS (*). Reply with clean, plain text only.";
        String result = null;

        String openAiKey = EnvConfig.getOpenAiKey();
        if (openAiKey != null && !openAiKey.trim().isEmpty()) {
            try {
                System.out.println("AIEngine: Attempting OpenAI GPT-4o-mini...");
                result = callOpenAI("gpt-4o-mini", enhancedSystemPrompt, userMessage, openAiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: OpenAI failed: " + formatError(e));
            }
        }

        if (result == null) {
            String mistralKey = EnvConfig.getMistralKey();
            if (mistralKey != null && !mistralKey.trim().isEmpty()) {
                try {
                    System.out.println("AIEngine: Falling back to Mistral Large...");
                    result = callMistral("mistral-large-latest", enhancedSystemPrompt, userMessage, mistralKey);
                } catch (Exception e) {
                    System.err.println("AIEngine: Mistral failed: " + formatError(e));
                }
            }
        }

        if (result == null) {
            String geminiKey = EnvConfig.getGeminiKey();
            if (geminiKey != null && !geminiKey.trim().isEmpty()) {
                try {
                    System.out.println("AIEngine: Falling back to Gemini 2.5 Flash...");
                    result = callGemini(enhancedSystemPrompt + "\n\n" + userMessage, geminiKey);
                } catch (Exception e) {
                    System.err.println("AIEngine: Gemini failed: " + formatError(e));
                }
            }
        }

        if (result != null) {
            return result.replace("*", "").trim();
        }

        return "ERROR: All configured AI providers failed or no valid API keys were found in environment/.env file.";
    }

    public static String processVision(String base64Image, String mimeType, String prompt) {
        String openAiKey = EnvConfig.getOpenAiKey();
        if (openAiKey != null && !openAiKey.trim().isEmpty()) {
            try {
                System.out.println("AIEngine: Attempting OpenAI Vision...");
                return callOpenAIVision("gpt-4o-mini", base64Image, mimeType, prompt, openAiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: OpenAI Vision failed: " + formatError(e));
            }
        }

        String mistralKey = EnvConfig.getMistralKey();
        if (mistralKey != null && !mistralKey.trim().isEmpty()) {
            try {
                System.out.println("AIEngine: Falling back to Mistral Pixtral...");
                return callMistralVision("pixtral-12b-2409", base64Image, mimeType, prompt, mistralKey);
            } catch (Exception e) {
                System.err.println("AIEngine: Mistral Vision failed: " + formatError(e));
            }
        }

        String geminiKey = EnvConfig.getGeminiKey();
        if (geminiKey != null && !geminiKey.trim().isEmpty()) {
            try {
                System.out.println("AIEngine: Falling back to Gemini Vision...");
                return callGeminiVision(base64Image, mimeType, prompt, geminiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: Gemini Vision failed: " + formatError(e));
            }
        }

        return "ERROR: All configured Vision providers failed or no API keys were found.";
    }

    public static String transcribeAudio(byte[] audioBytes, String mimeType) {
        String geminiKey = EnvConfig.getGeminiKey();
        if (geminiKey != null && !geminiKey.trim().isEmpty()) {
            try {
                System.out.println("AIEngine: Transcribing audio using Gemini...");
                String base64Audio = java.util.Base64.getEncoder().encodeToString(audioBytes);
                return callGeminiAudio(base64Audio, mimeType, geminiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: Gemini Audio transcription failed: " + formatError(e));
            }
        }

        String openAiKey = EnvConfig.getOpenAiKey();
        if (openAiKey != null && !openAiKey.trim().isEmpty()) {
            try {
                System.out.println("AIEngine: Transcribing audio using OpenAI Whisper...");
                return callOpenAIWhisper(audioBytes, openAiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: OpenAI Whisper failed: " + formatError(e));
            }
        }

        return "ERROR: Speech-to-text failed. Please configure GEMINI_API_KEY or OPENAI_API_KEY in .env";
    }

    private static String callGeminiAudio(String base64Audio, String mimeType, String geminiKey) throws Exception {
        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", "Transcribe this user voice audio recording verbatim into clean plain text. Return ONLY the transcribed text. If no audible speech is detected or if only silence/noise is present, return: Error: No speech detected");
        parts.add(textPart);

        JsonObject inlineDataPart = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", mimeType);
        inlineData.addProperty("data", base64Audio);
        inlineDataPart.add("inline_data", inlineData);
        parts.add(inlineDataPart);

        contentObj.add("parts", parts);
        contents.add(contentObj);
        root.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiKey))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "Gemini Audio");
        return extractGeminiText(response.body()).trim();
    }

    private static String callOpenAIWhisper(byte[] audioBytes, String openAiKey) throws Exception {
        String boundary = "----FinvoraBoundary" + System.currentTimeMillis();
        byte[] lineEnd = "\r\n".getBytes(StandardCharsets.UTF_8);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write("whisper-1\r\n".getBytes(StandardCharsets.UTF_8));

        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write("Content-Type: audio/wav\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        baos.write(audioBytes);
        baos.write(lineEnd);

        baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/audio/transcriptions"))
                .header("Authorization", "Bearer " + openAiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "OpenAI Whisper");
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.has("text") ? json.get("text").getAsString().trim() : "";
    }

    // --- GEMINI ---
    private static String callGemini(String prompt, String geminiKey) throws Exception {
        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject partObj = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        partObj.add("parts", parts);
        contents.add(partObj);
        root.add("contents", contents);

        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("temperature", 0.2);
        root.add("generationConfig", genConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiKey))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "Gemini");
        return extractGeminiText(response.body());
    }

    private static String callGeminiVision(String base64Image, String mimeType, String prompt, String geminiKey) throws Exception {
        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);

        JsonObject inlineDataPart = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", mimeType);
        inlineData.addProperty("data", base64Image);
        inlineDataPart.add("inline_data", inlineData);
        parts.add(inlineDataPart);

        contentObj.add("parts", parts);
        contents.add(contentObj);
        root.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiKey))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "Gemini Vision");
        return extractGeminiText(response.body());
    }

    // --- MISTRAL ---
    private static String callMistral(String model, String sys, String user, String mistralKey) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.addProperty("temperature", 0.2);

        JsonArray messages = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", sys);
        messages.add(sysMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", user);
        messages.add(userMsg);

        payload.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                .header("Authorization", "Bearer " + mistralKey)
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "Mistral");
        return extractOaiContent(response.body());
    }

    private static String callMistralVision(String model, String base64Image, String mimeType, String prompt, String mistralKey) throws Exception {
        return callOaiCompatibleVision("https://api.mistral.ai/v1/chat/completions", mistralKey, model, base64Image, mimeType, prompt);
    }

    // --- OPENAI ---
    private static String callOpenAI(String model, String sys, String user, String openAiKey) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.addProperty("temperature", 0.2);

        JsonArray messages = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", sys);
        messages.add(sysMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", user);
        messages.add(userMsg);

        payload.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + openAiKey)
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "OpenAI");
        return extractOaiContent(response.body());
    }

    private static String callOpenAIVision(String model, String base64Image, String mimeType, String prompt, String openAiKey) throws Exception {
        return callOaiCompatibleVision("https://api.openai.com/v1/chat/completions", openAiKey, model, base64Image, mimeType, prompt);
    }

    // --- UTILS ---
    private static String callOaiCompatibleVision(String url, String key, String model, String base64Image, String mimeType, String prompt) throws Exception {
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;
        JsonObject root = new JsonObject();
        root.addProperty("model", model);

        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");

        JsonArray contentArray = new JsonArray();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("type", "text");
        textObj.addProperty("text", prompt);
        contentArray.add(textObj);

        JsonObject imageObj = new JsonObject();
        imageObj.addProperty("type", "image_url");
        JsonObject imageUrlObj = new JsonObject();
        imageUrlObj.addProperty("url", dataUrl);
        imageObj.add("image_url", imageUrlObj);
        contentArray.add(imageObj);

        userMessage.add("content", contentArray);
        messages.add(userMessage);
        root.add("messages", messages);
        root.addProperty("max_tokens", 500);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + key)
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "Vision Provider");
        return extractOaiContent(response.body());
    }

    private static void checkResponseStatus(HttpResponse<String> response, String provider) throws Exception {
        int code = response.statusCode();
        if (code == 200) return;
        if (code == 401) throw new Exception(provider + " Unauthorized (HTTP 401): Invalid API Key.");
        if (code == 429) throw new Exception(provider + " Rate Limited (HTTP 429): Quota exceeded or too many requests.");
        if (code >= 500) throw new Exception(provider + " Server Error (HTTP " + code + "): " + response.body());
        throw new Exception(provider + " Error (HTTP " + code + "): " + response.body());
    }

    private static String extractOaiContent(String jsonBody) {
        try {
            JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject choice = choices.get(0).getAsJsonObject();
                JsonObject message = choice.getAsJsonObject("message");
                if (message != null && message.has("content")) {
                    return message.get("content").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static String extractGeminiText(String jsonBody) {
        try {
            JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject candidate = candidates.get(0).getAsJsonObject();
                JsonObject content = candidate.getAsJsonObject("content");
                if (content != null && content.has("parts")) {
                    JsonArray parts = content.getAsJsonArray("parts");
                    if (parts.size() > 0) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static String formatError(Exception e) {
        if (e instanceof HttpTimeoutException) {
            return "Request timed out after " + TIMEOUT.getSeconds() + "s";
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
