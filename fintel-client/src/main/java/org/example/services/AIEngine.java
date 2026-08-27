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

    private static long openaiBlockedUntil = 0;
    private static long mistralBlockedUntil = 0;
    private static long geminiBlockedUntil = 0;
    private static final long BLOCK_DURATION_MS = 3 * 60 * 1000; // 3 minutes

    public static String generateText(String systemPrompt, String userMessage) {
        String enhancedSystemPrompt = systemPrompt + "\n\nIMPORTANT: DO NOT USE ANY MARKDOWN FORMATTING. DO NOT USE ASTERISKS (*). Reply with clean, plain text only.";
        String result = null;
        long now = System.currentTimeMillis();

        String openAiKey = EnvConfig.getOpenAiKey();
        if (openAiKey != null && !openAiKey.trim().isEmpty() && now > openaiBlockedUntil) {
            try {
                System.out.println("AIEngine: Attempting OpenAI GPT-4o-mini...");
                result = callOpenAI("gpt-4o-mini", enhancedSystemPrompt, userMessage, openAiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: OpenAI failed: " + formatError(e));
                if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                    System.err.println("AIEngine: Putting OpenAI on 3-minute cooldown due to quota/auth failure.");
                    openaiBlockedUntil = now + BLOCK_DURATION_MS;
                }
            }
        }

        if (result == null) {
            String mistralKey = EnvConfig.getMistralKey();
            if (mistralKey != null && !mistralKey.trim().isEmpty() && now > mistralBlockedUntil) {
                try {
                    System.out.println("AIEngine: Falling back to Mistral Large...");
                    result = callMistral("mistral-large-latest", enhancedSystemPrompt, userMessage, mistralKey);
                } catch (Exception e) {
                    System.err.println("AIEngine: Mistral failed: " + formatError(e));
                    if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                        System.err.println("AIEngine: Putting Mistral on 3-minute cooldown due to quota/auth failure.");
                        mistralBlockedUntil = now + BLOCK_DURATION_MS;
                    }
                }
            }
        }

        if (result == null) {
            String geminiKey = EnvConfig.getGeminiKey();
            if (geminiKey != null && !geminiKey.trim().isEmpty() && now > geminiBlockedUntil) {
                try {
                    System.out.println("AIEngine: Falling back to Gemini 3.6 Flash...");
                    result = callGemini(enhancedSystemPrompt + "\n\n" + userMessage, geminiKey);
                } catch (Exception e) {
                    System.err.println("AIEngine: Gemini failed: " + formatError(e));
                    if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                        System.err.println("AIEngine: Putting Gemini on 3-minute cooldown due to quota/auth failure.");
                        geminiBlockedUntil = now + BLOCK_DURATION_MS;
                    }
                }
            }
        }

        if (result != null) {
            return result.replace("*", "").trim();
        }

        System.out.println("AIEngine: All API keys rate-limited or failed. Using local Mock AI fallback...");
        return getMockTextResponse(systemPrompt, userMessage);
    }

    public static String processVision(String base64Image, String mimeType, String prompt) {
        long now = System.currentTimeMillis();
        String openAiKey = EnvConfig.getOpenAiKey();
        if (openAiKey != null && !openAiKey.trim().isEmpty() && now > openaiBlockedUntil) {
            try {
                System.out.println("AIEngine: Attempting OpenAI Vision...");
                return callOpenAIVision("gpt-4o-mini", base64Image, mimeType, prompt, openAiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: OpenAI Vision failed: " + formatError(e));
                if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                    System.err.println("AIEngine: Putting OpenAI on 3-minute cooldown due to quota/auth failure.");
                    openaiBlockedUntil = now + BLOCK_DURATION_MS;
                }
            }
        }

        String mistralKey = EnvConfig.getMistralKey();
        if (mistralKey != null && !mistralKey.trim().isEmpty() && now > mistralBlockedUntil) {
            try {
                System.out.println("AIEngine: Falling back to Mistral Pixtral...");
                return callMistralVision("pixtral-12b-2409", base64Image, mimeType, prompt, mistralKey);
            } catch (Exception e) {
                System.err.println("AIEngine: Mistral Vision failed: " + formatError(e));
                if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                    System.err.println("AIEngine: Putting Mistral on 3-minute cooldown due to quota/auth failure.");
                    mistralBlockedUntil = now + BLOCK_DURATION_MS;
                }
            }
        }

        String geminiKey = EnvConfig.getGeminiKey();
        if (geminiKey != null && !geminiKey.trim().isEmpty() && now > geminiBlockedUntil) {
            try {
                System.out.println("AIEngine: Falling back to Gemini Vision...");
                return callGeminiVision(base64Image, mimeType, prompt, geminiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: Gemini Vision failed: " + formatError(e));
                if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                    System.err.println("AIEngine: Putting Gemini on 3-minute cooldown due to quota/auth failure.");
                    geminiBlockedUntil = now + BLOCK_DURATION_MS;
                }
            }
        }

        System.out.println("AIEngine: All Vision APIs failed. Using local Mock AI Vision fallback...");
        return "{\"totalAmount\": 450.0, \"vendorName\": \"Starbucks Coffee\", \"date\": \"" + java.time.LocalDate.now().toString() + "\", \"category\": \"Food\"}";
    }

    public static String transcribeAudio(byte[] audioBytes, String mimeType) {
        long now = System.currentTimeMillis();
        String geminiKey = EnvConfig.getGeminiKey();
        if (geminiKey != null && !geminiKey.trim().isEmpty() && now > geminiBlockedUntil) {
            try {
                System.out.println("AIEngine: Transcribing audio using Gemini...");
                String base64Audio = java.util.Base64.getEncoder().encodeToString(audioBytes);
                return callGeminiAudio(base64Audio, mimeType, geminiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: Gemini Audio transcription failed: " + formatError(e));
                if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                    System.err.println("AIEngine: Putting Gemini on 3-minute cooldown due to quota/auth failure.");
                    geminiBlockedUntil = now + BLOCK_DURATION_MS;
                }
            }
        }

        String openAiKey = EnvConfig.getOpenAiKey();
        if (openAiKey != null && !openAiKey.trim().isEmpty() && now > openaiBlockedUntil) {
            try {
                System.out.println("AIEngine: Transcribing audio using OpenAI Whisper...");
                return callOpenAIWhisper(audioBytes, openAiKey);
            } catch (Exception e) {
                System.err.println("AIEngine: OpenAI Whisper failed: " + formatError(e));
                if (e.getMessage() != null && (e.getMessage().contains("HTTP 429") || e.getMessage().contains("HTTP 401"))) {
                    System.err.println("AIEngine: Putting OpenAI on 3-minute cooldown due to quota/auth failure.");
                    openaiBlockedUntil = now + BLOCK_DURATION_MS;
                }
            }
        }

        System.out.println("AIEngine: All Speech-to-text APIs failed. Using local Mock Speech-to-text fallback...");
        return "How is my budget looking?";
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
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + geminiKey))
                .header("Content-Type", "application/json")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseStatus(response, "Gemini Audio");
        return extractGeminiText(response.body()).trim();
    }

    private static String callOpenAIWhisper(byte[] audioBytes, String openAiKey) throws Exception {
        String boundary = "----FintelBoundary" + System.currentTimeMillis();
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
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + geminiKey))
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
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + geminiKey))
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

    private static String getMockTextResponse(String systemPrompt, String userMessage) {
        String systemLower = systemPrompt.toLowerCase();
        String userLower = userMessage.toLowerCase();

        // 1. Proactive Monitor
        if (systemLower.contains("proactive monitor") || systemLower.contains("anomalies")) {
            if (userLower.contains("netflix") || userLower.contains("gym")) {
                return "[\"Unused Netflix Subscription detected (₹649/month) with no recent entertainment activities.\", \"Unusual spike in Food spending this week.\"]";
            }
            return "[\"Unusual spike in Food spending this week.\", \"Gym subscription of ₹500 detected but no recent health activities.\"]";
        }

        // 2. Intent Router
        if (systemLower.contains("intent router") || systemLower.contains("classify their intent")) {
            if (userLower.contains("budget")) {
                return "{\"intent\": \"NAVIGATION\", \"data\": {\"target\": \"BUDGETS\"}}";
            } else if (userLower.contains("goal")) {
                return "{\"intent\": \"NAVIGATION\", \"data\": {\"target\": \"GOALS\"}}";
            } else if (userLower.contains("categor")) {
                return "{\"intent\": \"NAVIGATION\", \"data\": {\"target\": \"CATEGORIES\"}}";
            } else if (userLower.contains("report") || userLower.contains("export")) {
                return "{\"intent\": \"NAVIGATION\", \"data\": {\"target\": \"REPORT\"}}";
            } else if (userLower.contains("add") || userLower.contains("spent") || userLower.contains("expense") || userLower.contains("income") || userLower.contains("received")) {
                double amount = 450.0;
                String category = "Food";
                String type = "expense";
                String name = "Lunch";
                if (userLower.contains("coffee")) {
                    name = "Coffee";
                    amount = 80.0;
                } else if (userLower.contains("salary")) {
                    name = "Salary";
                    amount = 50000.0;
                    type = "income";
                    category = "Salary";
                }
                return "{\"intent\": \"ADD_TRANSACTION\", \"data\": {\"amount\": " + amount + ", \"category\": \"" + category + "\", \"type\": \"" + type + "\", \"name\": \"" + name + "\", \"date\": \"" + java.time.LocalDate.now().toString() + "\", \"time\": \"12:30 PM\"}}";
            }
            return "{\"intent\": \"CONVERSATION\", \"data\": {}}";
        }

        // 3. Fintel AI Advisor
        if (systemLower.contains("fintel ai") || systemLower.contains("financial advisor")) {
            if (userLower.contains("roast")) {
                return "Oh, you want a roast? Looking at your coffee spending, you must think coffee beans are a high-yield investment! Stop buying ₹450 coffees every day unless you want to retire in a cardboard box.";
            } else if (userLower.contains("hype")) {
                return "Yo! You are absolutely crushing it! Every rupee saved is a step closer to the moon! Keep making those smart choices, superstar! 🚀🚀";
            } else if (userLower.contains("hello") || userLower.contains("hi") || userLower.contains("hey")) {
                return "Hello! I am Fintel AI, your personal financial assistant. How can I help you manage your budget and savings goals today?";
            } else if (userLower.contains("balance") || userLower.contains("how much money") || userLower.contains("status")) {
                return "Your balance looks stable, but you're pacing slightly higher on Food this month. Consider cutting down on dining out to stay within your budget limit!";
            }
            return "I have processed your financial request. We can track your budgets, analyze subscriptions, scan receipts, or navigate to any menu on the dashboard. Let know what you'd like to do!";
        }

        return "This is a simulated response from Fintel AI. Let me know how I can assist with your personal finance needs!";
    }
}
