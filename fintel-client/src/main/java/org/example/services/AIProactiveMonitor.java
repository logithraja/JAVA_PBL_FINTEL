package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.models.Transaction;
import org.example.models.Budget;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AIProactiveMonitor {
    private static List<String> cachedInsights = null;
    private static long lastAnalysisTime = 0;
    private static int lastTransactionCount = -1;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutes

    public static List<String> analyzeTransactions(List<Transaction> transactions, List<Budget> budgets) {
        List<String> alerts = new ArrayList<>();
        
        // 1. Guaranteed Budget Checking (No AI hallucination risk)
        if (budgets != null) {
            for (Budget b : budgets) {
                if (b.getSpentAmount().compareTo(b.getLimitAmount()) > 0) {
                    alerts.add("🚨 BUDGET EXCEEDED: You have spent ₹" + b.getSpentAmount() + " on " + b.getCategory() + " (Limit: ₹" + b.getLimitAmount() + ") for " + b.getPeriodLabel());
                }
            }
        }

        if (transactions == null || transactions.isEmpty()) return alerts;

        // Check Cache
        long currentTime = System.currentTimeMillis();
        int txCount = transactions.size();
        if (cachedInsights != null && (currentTime - lastAnalysisTime < CACHE_DURATION_MS) && txCount == lastTransactionCount) {
            System.out.println("AIProactiveMonitor: Using cached AI insights to save quota...");
            List<String> combined = new ArrayList<>(alerts);
            combined.addAll(cachedInsights);
            return combined;
        }
        
        StringBuilder data = new StringBuilder();
        int max = Math.min(transactions.size(), 50);
        for (int i=0; i<max; i++) {
            Transaction t = transactions.get(i);
            String catName = (t.getTransactionCategory() != null) ? t.getTransactionCategory().getCategoryName() : "Uncategorized";
            data.append(t.getTransactionDate()).append(" | ")
                .append(t.getTransactionName()).append(" | $")
                .append(t.getTransactionAmount()).append(" | ")
                .append(catName).append("\n");
        }
        
        String systemMessage = "You are an AI Proactive Monitor. Analyze the following transactions and identify any unused recurring subscriptions or unusual spending anomalies. Return ONLY a RAW JSON ARRAY of strings containing your specific warnings. Do not use markdown blocks. Example: [\"Unusual spike in Food spending this week.\", \"Gym subscription of ₹500 detected but no recent health activities.\"]. ALWAYS use the Indian Rupee symbol (₹) for currency amounts, NEVER use $.";
        
        try {
            String content = AIEngine.generateText(systemMessage, data.toString());
            
            if (content.startsWith("ERROR")) {
                System.err.println(content);
                return alerts;
            }

            if (content.startsWith("```json")) {
                content = content.substring(7);
                if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
                if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
            }
            
            try {
                JsonArray jsonArray = JsonParser.parseString(content.trim()).getAsJsonArray();
                List<String> insights = new ArrayList<>();
                for (JsonElement el : jsonArray) {
                    String insight = "💡 Insight: " + el.getAsString();
                    insights.add(insight);
                    alerts.add(insight);
                }
                
                // Cache successful AI insights
                cachedInsights = insights;
                lastAnalysisTime = currentTime;
                lastTransactionCount = txCount;
            } catch (Exception parseEx) {
                System.err.println("Failed to parse AI response: " + content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alerts;
    }
}
