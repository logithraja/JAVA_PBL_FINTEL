package org.example.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.models.Transaction;
import org.example.models.TransactionCategory;
import org.example.models.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class CsvImportUtil {

    private CsvImportUtil() {}

    public static class ImportResult {
        public final JsonArray jsonPayload = new JsonArray();
        public int totalRows = 0;
        public int importedCount = 0;
        public int duplicatesSkipped = 0;
        public final List<String> errors = new ArrayList<>();
    }

    public static ImportResult parseAndPrepare(File file, User user, List<TransactionCategory> availableCategories) {
        ImportResult result = new ImportResult();
        if (file == null || !file.exists()) {
            result.errors.add("File does not exist");
            return result;
        }

        // Fetch recent transactions to perform duplicate detection
        List<Transaction> existingTransactions = ApiClient.getRecentTransactionByUserId(user.getId(), 0, 0, 500);
        if (existingTransactions == null) existingTransactions = Collections.emptyList();

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                result.errors.add("CSV file is empty");
                return result;
            }

            String[] headers = parseCsvLine(headerLine);
            Map<String, Integer> colMap = buildColumnMap(headers);

            String line;
            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;
                result.totalRows++;

                String[] tokens = parseCsvLine(line);
                try {
                    String name = getField(tokens, colMap, "name", "description", "merchant", "payee");
                    if (name.isBlank()) name = "Imported Transaction";

                    String dateStr = getField(tokens, colMap, "date", "transaction_date", "txn_date");
                    LocalDate date = parseDate(dateStr);

                    String amountStr = getField(tokens, colMap, "amount", "transaction_amount", "value");
                    double amount = Math.abs(Double.parseDouble(amountStr.replaceAll("[^0-9.-]", "")));
                    if (amount <= 0) continue;

                    String type = getField(tokens, colMap, "type", "transaction_type");
                    if (type.isBlank()) {
                        type = amountStr.startsWith("-") ? "EXPENSE" : "EXPENSE";
                    } else if (type.toUpperCase().contains("INC") || type.toUpperCase().contains("CR")) {
                        type = "INCOME";
                    } else {
                        type = "EXPENSE";
                    }

                    // Duplicate check
                    final String checkName = name;
                    final double checkAmount = amount;
                    final LocalDate checkDate = date;
                    boolean isDuplicate = existingTransactions.stream().anyMatch(et ->
                            et.getTransactionDate().equals(checkDate) &&
                            Math.abs(et.getTransactionAmount() - checkAmount) < 0.01 &&
                            et.getTransactionName().equalsIgnoreCase(checkName)
                    );

                    if (isDuplicate) {
                        result.duplicatesSkipped++;
                        continue;
                    }

                    // Auto-categorization
                    String categoryStr = getField(tokens, colMap, "category");
                    TransactionCategory matchedCat = autoMatchCategory(categoryStr, name, availableCategories);

                    JsonObject tObj = new JsonObject();
                    JsonObject uObj = new JsonObject();
                    uObj.addProperty("id", user.getId());
                    tObj.add("user", uObj);

                    if (matchedCat != null) {
                        JsonObject cObj = new JsonObject();
                        cObj.addProperty("id", matchedCat.getId());
                        cObj.addProperty("categoryName", matchedCat.getCategoryName());
                        cObj.addProperty("categoryColor", matchedCat.getCategoryColor());
                        tObj.add("transactionCategory", cObj);
                    }

                    tObj.addProperty("transactionName", name);
                    tObj.addProperty("transactionAmount", amount);
                    tObj.addProperty("transactionDate", date.toString());
                    tObj.addProperty("transactionType", type);

                    result.jsonPayload.add(tObj);
                    result.importedCount++;
                } catch (Exception e) {
                    result.errors.add("Line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.errors.add("Error reading CSV: " + e.getMessage());
        }

        return result;
    }

    private static TransactionCategory autoMatchCategory(String explicitCategory, String description, List<TransactionCategory> categories) {
        if (categories == null || categories.isEmpty()) return null;

        // 1. Match explicit category name
        if (explicitCategory != null && !explicitCategory.isBlank()) {
            for (TransactionCategory cat : categories) {
                if (cat.getCategoryName().equalsIgnoreCase(explicitCategory.trim())) {
                    return cat;
                }
            }
        }

        // 2. Keyword matching on description
        String lowerDesc = description.toLowerCase();
        for (TransactionCategory cat : categories) {
            String catLower = cat.getCategoryName().toLowerCase();
            if (lowerDesc.contains(catLower) || catLower.contains(lowerDesc)) {
                return cat;
            }
        }

        // 3. Known heuristics
        Map<String, String[]> heuristics = Map.of(
                "Food", new String[]{"swiggy", "zomato", "restaurant", "cafe", "coffee", "mcdonald", "starbucks", "grocery", "supermarket"},
                "Travel", new String[]{"uber", "ola", "metro", "fuel", "petrol", "flight", "irctc", "railway"},
                "Shopping", new String[]{"amazon", "flipkart", "myntra", "store", "mall", "clothing"},
                "Entertainment", new String[]{"netflix", "spotify", "hotstar", "prime", "cinema", "movie", "bookmyshow"}
        );

        for (Map.Entry<String, String[]> entry : heuristics.entrySet()) {
            for (String kw : entry.getValue()) {
                if (lowerDesc.contains(kw)) {
                    for (TransactionCategory cat : categories) {
                        if (cat.getCategoryName().toLowerCase().contains(entry.getKey().toLowerCase())) {
                            return cat;
                        }
                    }
                }
            }
        }

        // Fallback to first available category
        return categories.get(0);
    }

    private static Map<String, Integer> buildColumnMap(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].trim().toLowerCase(), i);
        }
        return map;
    }

    private static String getField(String[] tokens, Map<String, Integer> map, String... aliases) {
        for (String alias : aliases) {
            Integer idx = map.get(alias);
            if (idx != null && idx < tokens.length) {
                return tokens[idx].trim();
            }
        }
        return "";
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) return LocalDate.now();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        );
        for (DateTimeFormatter dtf : formatters) {
            try {
                return LocalDate.parse(text.trim(), dtf);
            } catch (Exception ignored) {}
        }
        return LocalDate.now();
    }

    private static String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}
