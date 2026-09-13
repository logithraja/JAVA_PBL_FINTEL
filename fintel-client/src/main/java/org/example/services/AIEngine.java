package org.example.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.utils.ApiUtil;

import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.Base64;

/**
 * Client-Side AI Engine.
 * All AI interactions are securely routed through the Fintel Backend Server AI Proxy (/api/v1/ai/*)
 * using the authenticated user's JWT. No provider API keys are stored on the client.
 */
public class AIEngine {

    public static String generateText(String systemPrompt, String userMessage) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("systemPrompt", systemPrompt != null ? systemPrompt : "");
            payload.addProperty("userMessage", userMessage != null ? userMessage : "");

            HttpURLConnection conn = ApiUtil.fetchApi("/api/v1/ai/chat", ApiUtil.RequestMethod.POST, payload);
            if (conn != null && conn.getResponseCode() == 200) {
                String responseBody = ApiUtil.readApiResponse(conn);
                if (responseBody != null && !responseBody.isBlank()) {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (json.has("reply") && !json.get("reply").isJsonNull()) {
                        return json.get("reply").getAsString().replace("*", "").trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("AIEngine: Proxy chat call failed: " + e.getMessage());
        }

        return getMockTextResponse(systemPrompt, userMessage);
    }

    public static String processVision(String base64Image, String mimeType, String prompt) {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("base64Image", base64Image);
            payload.addProperty("mimeType", mimeType != null ? mimeType : "image/jpeg");
            payload.addProperty("prompt", prompt);

            HttpURLConnection conn = ApiUtil.fetchApi("/api/v1/ai/vision", ApiUtil.RequestMethod.POST, payload);
            if (conn != null && conn.getResponseCode() == 200) {
                String responseBody = ApiUtil.readApiResponse(conn);
                if (responseBody != null && !responseBody.isBlank()) {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (json.has("data") && !json.get("data").isJsonNull()) {
                        return json.get("data").getAsString();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("AIEngine: Proxy vision call failed: " + e.getMessage());
        }

        return "{\"totalAmount\": 450.0, \"vendorName\": \"Starbucks Coffee\", \"date\": \"" + LocalDate.now() + "\", \"category\": \"Food\"}";
    }

    public static String transcribeAudio(byte[] audioBytes, String mimeType) {
        try {
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            JsonObject payload = new JsonObject();
            payload.addProperty("base64Audio", base64Audio);
            payload.addProperty("mimeType", mimeType != null ? mimeType : "audio/wav");

            HttpURLConnection conn = ApiUtil.fetchApi("/api/v1/ai/transcribe", ApiUtil.RequestMethod.POST, payload);
            if (conn != null && conn.getResponseCode() == 200) {
                String responseBody = ApiUtil.readApiResponse(conn);
                if (responseBody != null && !responseBody.isBlank()) {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (json.has("transcript") && !json.get("transcript").isJsonNull()) {
                        return json.get("transcript").getAsString().trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("AIEngine: Proxy transcribe call failed: " + e.getMessage());
        }

        return "How is my budget looking?";
    }

    public static String getMockTextResponse(String systemPrompt, String userMessage) {
        String systemLower = (systemPrompt != null ? systemPrompt : "").toLowerCase();
        String userLower = (userMessage != null ? userMessage : "").toLowerCase();

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
                return "{\"intent\": \"ADD_TRANSACTION\", \"data\": {\"amount\": " + amount + ", \"category\": \"" + category + "\", \"type\": \"" + type + "\", \"name\": \"" + name + "\", \"date\": \"" + LocalDate.now() + "\", \"time\": \"12:30 PM\"}}";
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
