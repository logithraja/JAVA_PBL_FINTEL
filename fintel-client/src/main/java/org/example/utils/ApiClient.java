package org.example.utils;

import com.google.gson.*;
import javafx.scene.control.Alert;
import org.example.models.SavingsGoal;
import org.example.models.Transaction;
import org.example.models.TransactionCategory;
import org.example.models.User;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    public static User getUserByEmail(String userEmail) {
        HttpURLConnection conn = null;
        try {
            String encodedEmail = URLEncoder.encode(userEmail, StandardCharsets.UTF_8);
            conn = ApiUtil.fetchApi(
                    "/api/v1/user?email=" + encodedEmail,
                    ApiUtil.RequestMethod.GET, null
            );
            if (conn == null) return null;

            if (conn.getResponseCode() != 200) {
                System.out.println("Error(getUserByEmail): " + conn.getResponseCode());
                return null;
            }

            String userDataJson = ApiUtil.readApiResponse(conn);
            if (userDataJson == null) return null;

            JsonObject jsonObject = JsonParser.parseString(userDataJson).getAsJsonObject();

            int id = jsonObject.get("id").getAsInt();
            String name = jsonObject.has("name") && !jsonObject.get("name").isJsonNull() ? jsonObject.get("name").getAsString() : "";
            String email = jsonObject.has("email") && !jsonObject.get("email").isJsonNull() ? jsonObject.get("email").getAsString() : userEmail;
            String password = jsonObject.has("password") && !jsonObject.get("password").isJsonNull() ? jsonObject.get("password").getAsString() : "";

            LocalDateTime createdAt = LocalDateTime.now();
            if (jsonObject.has("createdAt") && !jsonObject.get("createdAt").isJsonNull()) {
                try {
                    createdAt = LocalDateTime.parse(jsonObject.get("createdAt").getAsString());
                } catch (Exception ignored) {}
            } else if (jsonObject.has("created_at") && !jsonObject.get("created_at").isJsonNull()) {
                try {
                    createdAt = LocalDateTime.parse(jsonObject.get("created_at").getAsString());
                } catch (Exception ignored) {}
            }

            return new User(id, name, email, password, createdAt);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return null;
    }

    public static boolean checkEmailExists(String email) {
        HttpURLConnection conn = null;
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            conn = ApiUtil.fetchApi(
                    "/api/v1/user/exists?email=" + encodedEmail,
                    ApiUtil.RequestMethod.GET, null
            );
            if (conn == null) return false;

            if (conn.getResponseCode() == 200) {
                String responseJson = ApiUtil.readApiResponse(conn);
                if (responseJson != null) {
                    JsonObject jsonObject = JsonParser.parseString(responseJson).getAsJsonObject();
                    return jsonObject.has("exists") && jsonObject.get("exists").getAsBoolean();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    public static boolean postLoginUser(String email, String password) {
        HttpURLConnection conn = null;
        try {
            JsonObject loginJson = new JsonObject();
            loginJson.addProperty("email", email);
            loginJson.addProperty("password", password);

            conn = ApiUtil.fetchApi(
                    "/api/v1/user/login",
                    ApiUtil.RequestMethod.POST,
                    loginJson
            );
            if (conn == null) return false;

            if (conn.getResponseCode() == 200) {
                String responseBody = ApiUtil.readApiResponse(conn);
                if (responseBody != null) {
                    JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (json.has("token") && !json.get("token").isJsonNull()) {
                        String token = json.get("token").getAsString();
                        ApiUtil.setAuthToken(token);
                    }
                    if (json.has("refreshToken") && !json.get("refreshToken").isJsonNull()) {
                        String refreshToken = json.get("refreshToken").getAsString();
                        ApiUtil.setRefreshToken(refreshToken);
                    }
                    return true;
                }
                return true;
            }

            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static void logoutUser() {
        try {
            String refreshToken = ApiUtil.getRefreshToken();
            if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                JsonObject payload = new JsonObject();
                payload.addProperty("refreshToken", refreshToken);
                HttpURLConnection conn = ApiUtil.fetchApi("/api/v1/user/logout", ApiUtil.RequestMethod.POST, payload);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } catch (Exception ignored) {}
        ApiUtil.clearTokens();
    }

    public static boolean postCreateUser(JsonObject userData) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/user",
                    ApiUtil.RequestMethod.POST,
                    userData
            );
            if (conn == null) return false;

            return conn.getResponseCode() == 201 || conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    public static List<Transaction> checkDuplicateTransaction(int userId, String merchant, double amount, LocalDate date) {
        List<Transaction> duplicates = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            String encodedMerchant = URLEncoder.encode(merchant != null ? merchant : "", StandardCharsets.UTF_8);
            String dateStr = date != null ? date.toString() : LocalDate.now().toString();
            String path = "/api/v1/transaction/duplicate-check?userId=" + userId +
                    "&merchant=" + encodedMerchant +
                    "&amount=" + amount +
                    "&date=" + dateStr;

            conn = ApiUtil.fetchApi(path, ApiUtil.RequestMethod.GET, null);
            if (conn == null) return duplicates;

            if (conn.getResponseCode() == 200) {
                String results = ApiUtil.readApiResponse(conn);
                if (results != null) {
                    JsonArray array = JsonParser.parseString(results).getAsJsonArray();
                    for (JsonElement el : array) {
                        JsonObject obj = el.getAsJsonObject();
                        int id = obj.get("id").getAsInt();
                        String name = obj.get("transactionName").getAsString();
                        double amt = obj.get("transactionAmount").getAsDouble();
                        LocalDate d = LocalDate.parse(obj.get("transactionDate").getAsString());
                        String type = obj.get("transactionType").getAsString();

                        TransactionCategory cat = null;
                        if (obj.has("transactionCategory") && !obj.get("transactionCategory").isJsonNull()) {
                            JsonObject catObj = obj.get("transactionCategory").getAsJsonObject();
                            cat = new TransactionCategory(catObj.get("id").getAsInt(),
                                    catObj.get("categoryName").getAsString(),
                                    catObj.get("categoryColor").getAsString());
                        }

                        duplicates.add(new Transaction(id, cat, name, amt, d, null, type));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ApiClient.checkDuplicateTransaction error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return duplicates;
    }

    public static JsonObject getSpendingForecast(int userId, int year, int month, Double budget) {
        HttpURLConnection conn = null;
        try {
            String path = "/api/v1/transaction/forecast/" + userId + "?year=" + year + "&month=" + month;
            if (budget != null && budget > 0) {
                path += "&budget=" + budget;
            }

            conn = ApiUtil.fetchApi(path, ApiUtil.RequestMethod.GET, null);
            if (conn == null) return null;

            if (conn.getResponseCode() == 200) {
                String results = ApiUtil.readApiResponse(conn);
                if (results != null) {
                    return JsonParser.parseString(results).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            System.err.println("ApiClient.getSpendingForecast error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static List<TransactionCategory> getAllTransactionCategoriesByUser(User user) {
        if (user == null) return new ArrayList<>();
        List<TransactionCategory> categories = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/user/" + user.getId(),
                    ApiUtil.RequestMethod.GET, null
            );
            if (conn == null) return null;

            if (conn.getResponseCode() != 200) {
                System.out.println("Error(getAllTransactionCategoriesByUser): " + conn.getResponseCode());
                return categories;
            }

            String result = ApiUtil.readApiResponse(conn);
            if (result == null) return categories;

            JsonArray resultJsonArray = JsonParser.parseString(result).getAsJsonArray();

            for (JsonElement jsonElement : resultJsonArray) {
                int categoryId = jsonElement.getAsJsonObject().get("id").getAsInt();
                String categoryName = jsonElement.getAsJsonObject().get("categoryName").getAsString();
                String categoryColor = jsonElement.getAsJsonObject().get("categoryColor").getAsString();

                categories.add(new TransactionCategory(categoryId, categoryName, categoryColor));
            }

            return categories;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return categories;
    }

    public static List<Transaction> getRecentTransactionByUserId(int userId, int startPage, int endPage, int size) {
        List<Transaction> recentTransactions = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/recent/user/" + userId +
                            "?startPage=" + startPage + "&endPage=" + endPage + "&size=" + size,
                    ApiUtil.RequestMethod.GET,
                    null
            );
            if (conn == null) return null;

            if (conn.getResponseCode() != 200) {
                return recentTransactions;
            }

            String results = ApiUtil.readApiResponse(conn);
            if (results == null) return recentTransactions;

            JsonArray resultJsonArray = JsonParser.parseString(results).getAsJsonArray();
            for (int i = 0; i < resultJsonArray.size(); i++) {
                JsonObject transactionJsonObj = resultJsonArray.get(i).getAsJsonObject();
                int transactionId = transactionJsonObj.get("id").getAsInt();

                TransactionCategory transactionCategory = null;
                if (transactionJsonObj.has("transactionCategory")
                        && !transactionJsonObj.get("transactionCategory").isJsonNull()) {
                    JsonObject catObj = transactionJsonObj.get("transactionCategory").getAsJsonObject();
                    int catId = catObj.get("id").getAsInt();
                    String catName = catObj.get("categoryName").getAsString();
                    String catColor = catObj.get("categoryColor").getAsString();

                    transactionCategory = new TransactionCategory(catId, catName, catColor);
                }

                String transactionName = transactionJsonObj.get("transactionName").getAsString();
                double transactionAmount = transactionJsonObj.get("transactionAmount").getAsDouble();
                LocalDate transactionDate = LocalDate.parse(transactionJsonObj.get("transactionDate").getAsString());
                String transactionTime = null;
                if (transactionJsonObj.has("transactionTime") && !transactionJsonObj.get("transactionTime").isJsonNull()) {
                    transactionTime = transactionJsonObj.get("transactionTime").getAsString();
                }
                String transactionType = transactionJsonObj.get("transactionType").getAsString();

                Transaction transaction = new Transaction(
                        transactionId,
                        transactionCategory,
                        transactionName,
                        transactionAmount,
                        transactionDate,
                        transactionTime,
                        transactionType
                );

                recentTransactions.add(transaction);
            }

            return recentTransactions;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return recentTransactions;
    }

    public static List<Transaction> getAllTransactionsByUserId(int userId, int year, Integer month) {
        List<Transaction> transactions = new ArrayList<>();
        HttpURLConnection conn = null;
        String apiPath = "/api/v1/transaction/user/" + userId + "?year=" + year;
        if (month != null) apiPath += "&month=" + month;

        try {
            conn = ApiUtil.fetchApi(apiPath, ApiUtil.RequestMethod.GET, null);
            if (conn == null) return null;

            if (conn.getResponseCode() != 200) {
                return transactions;
            }

            String results = ApiUtil.readApiResponse(conn);
            if (results == null) return transactions;

            JsonArray resultJson = JsonParser.parseString(results).getAsJsonArray();

            for (int i = 0; i < resultJson.size(); i++) {
                JsonObject transactionJsonObj = resultJson.get(i).getAsJsonObject();
                int transactionId = transactionJsonObj.get("id").getAsInt();

                TransactionCategory transactionCategory = null;
                if (transactionJsonObj.has("transactionCategory")
                        && !transactionJsonObj.get("transactionCategory").isJsonNull()) {
                    JsonObject catObj = transactionJsonObj.get("transactionCategory").getAsJsonObject();
                    int catId = catObj.get("id").getAsInt();
                    String catName = catObj.get("categoryName").getAsString();
                    String catColor = catObj.get("categoryColor").getAsString();

                    transactionCategory = new TransactionCategory(catId, catName, catColor);
                }

                String transactionName = transactionJsonObj.get("transactionName").getAsString();
                double transactionAmount = transactionJsonObj.get("transactionAmount").getAsDouble();
                LocalDate transactionDate = LocalDate.parse(transactionJsonObj.get("transactionDate").getAsString());
                String transactionTime = null;
                if (transactionJsonObj.has("transactionTime") && !transactionJsonObj.get("transactionTime").isJsonNull()) {
                    transactionTime = transactionJsonObj.get("transactionTime").getAsString();
                }
                String transactionType = transactionJsonObj.get("transactionType").getAsString();

                Transaction transaction = new Transaction(
                        transactionId,
                        transactionCategory,
                        transactionName,
                        transactionAmount,
                        transactionDate,
                        transactionTime,
                        transactionType
                );

                transactions.add(transaction);
            }

            return transactions;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return transactions;
    }

    public static List<Integer> getAllDistinctYears(int userId) {
        List<Integer> distinctYears = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/years/" + userId,
                    ApiUtil.RequestMethod.GET, null
            );
            if (conn == null) return null;

            if (conn.getResponseCode() != 200) {
                System.out.println("Error(getAllDistinctYears): " + conn.getResponseCode());
                return distinctYears;
            }

            String result = ApiUtil.readApiResponse(conn);
            if (result == null) return distinctYears;

            JsonArray resultsArray = JsonParser.parseString(result).getAsJsonArray();
            for (int i = 0; i < resultsArray.size(); i++) {
                int year = resultsArray.get(i).getAsInt();
                distinctYears.add(year);
            }

            return distinctYears;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return distinctYears;
    }

    public static boolean postTransactionCategory(JsonObject transactionCategoryData) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category",
                    ApiUtil.RequestMethod.POST,
                    transactionCategoryData
            );
            if (conn == null) return false;

            return conn.getResponseCode() == 201 || conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return false;
    }

    public static boolean postTransaction(JsonObject transactionData) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction",
                    ApiUtil.RequestMethod.POST,
                    transactionData
            );
            if (conn == null) return false;

            return conn.getResponseCode() == 201 || conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return false;
    }

    public static boolean putTransaction(JsonObject newTransactionData) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction",
                    ApiUtil.RequestMethod.PUT,
                    newTransactionData
            );
            if (conn == null) return false;

            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return false;
    }

    public static boolean putTransactionCategory(int categoryId, String newCategoryName, String newCategoryColor) {
        HttpURLConnection conn = null;
        String encodedCategoryName = URLEncoder.encode(newCategoryName, StandardCharsets.UTF_8);
        String encodedCategoryColor = URLEncoder.encode(newCategoryColor, StandardCharsets.UTF_8);

        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/" + categoryId + "?newCategoryName=" + encodedCategoryName +
                            "&newCategoryColor=" + encodedCategoryColor,
                    ApiUtil.RequestMethod.PUT,
                    null
            );
            if (conn == null) return false;

            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return false;
    }

    public static boolean deleteTransactionCategoryById(int categoryId) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/" + categoryId,
                    ApiUtil.RequestMethod.DELETE,
                    null
            );
            if (conn == null) return false;

            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return false;
    }

    public static boolean deleteTransactionById(int transactionId) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/" + transactionId,
                    ApiUtil.RequestMethod.DELETE,
                    null
            );
            if (conn == null) return false;

            return conn.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return false;
    }

    public static List<SavingsGoal> getSavingsGoals(int userId) {
        List<SavingsGoal> goals = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi("/api/v1/savings-goals/user/" + userId, ApiUtil.RequestMethod.GET, null);
            if (conn != null) {
                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    JsonArray array = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonArray();
                    for (JsonElement el : array) {
                        JsonObject obj = el.getAsJsonObject();
                        SavingsGoal goal = new SavingsGoal(
                                obj.get("name").getAsString(),
                                obj.get("targetAmount").getAsBigDecimal(),
                                obj.get("currentAmount").getAsBigDecimal(),
                                LocalDate.parse(obj.get("deadline").getAsString())
                        );
                        goal.setId(obj.get("id").getAsInt());
                        goals.add(goal);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return goals;
    }

    public static SavingsGoal postSavingsGoal(int userId, SavingsGoal goal) {
        HttpURLConnection conn = null;
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("name", goal.getName());
            payload.addProperty("targetAmount", goal.getTargetAmount());
            payload.addProperty("currentAmount", goal.getCurrentAmount());
            payload.addProperty("deadline", goal.getDeadline().toString());

            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", userId);
            payload.add("user", userObj);

            conn = ApiUtil.fetchApi("/api/v1/savings-goals", ApiUtil.RequestMethod.POST, payload);
            if (conn != null) {
                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    JsonObject obj = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
                    goal.setId(obj.get("id").getAsInt());
                    return goal;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static boolean putSavingsGoal(int userId, SavingsGoal goal) {
        HttpURLConnection conn = null;
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("id", goal.getId());
            payload.addProperty("name", goal.getName());
            payload.addProperty("targetAmount", goal.getTargetAmount());
            payload.addProperty("currentAmount", goal.getCurrentAmount());
            payload.addProperty("deadline", goal.getDeadline().toString());

            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", userId);
            payload.add("user", userObj);

            conn = ApiUtil.fetchApi("/api/v1/savings-goals", ApiUtil.RequestMethod.PUT, payload);
            if (conn != null) {
                return conn.getResponseCode() >= 200 && conn.getResponseCode() < 300;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    public static boolean deleteSavingsGoal(int goalId) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi("/api/v1/savings-goals/" + goalId, ApiUtil.RequestMethod.DELETE, null);
            return conn != null && (conn.getResponseCode() == 200 || conn.getResponseCode() == 204);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    public static com.google.gson.JsonObject simulateScenario(int userId, double amount, String category, int targetMonth, Integer targetYear) {
        HttpURLConnection conn = null;
        try {
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("userId", userId);
            payload.addProperty("amount", amount);
            payload.addProperty("category", category);
            payload.addProperty("targetMonth", targetMonth);
            if (targetYear != null) {
                payload.addProperty("targetYear", targetYear);
            }

            conn = ApiUtil.fetchApi("/api/v1/scenario/simulate", ApiUtil.RequestMethod.POST, payload);
            if (conn != null && conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                java.io.InputStream is = conn.getResponseCode() < 400 ? conn.getInputStream() : conn.getErrorStream();
                if (is != null) {
                    java.util.Scanner sc = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
                    String body = sc.hasNext() ? sc.next() : "";
                    sc.close();
                    return com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static com.google.gson.JsonObject getHealthScore(int userId) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi("/api/v1/health?userId=" + userId, ApiUtil.RequestMethod.GET, null);
            if (conn != null && conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                java.io.InputStream is = conn.getInputStream();
                if (is != null) {
                    java.util.Scanner sc = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
                    String body = sc.hasNext() ? sc.next() : "";
                    sc.close();
                    return com.google.gson.JsonParser.parseString(body).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static List<org.example.models.Budget> getBudgets(int userId) {
        List<org.example.models.Budget> list = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi("/api/v1/budgets/user/" + userId, ApiUtil.RequestMethod.GET, null);
            if (conn != null && conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                JsonArray array = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonArray();
                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    org.example.models.Budget b = new org.example.models.Budget();
                    b.setId(obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsLong() : null);
                    b.setCategory(obj.has("category") && !obj.get("category").isJsonNull() ? obj.get("category").getAsString() : null);
                    b.setLimitAmount(obj.has("limitAmount") && !obj.get("limitAmount").isJsonNull() ? new java.math.BigDecimal(obj.get("limitAmount").getAsString()) : java.math.BigDecimal.ZERO);
                    b.setSpentAmount(obj.has("spentAmount") && !obj.get("spentAmount").isJsonNull() ? new java.math.BigDecimal(obj.get("spentAmount").getAsString()) : java.math.BigDecimal.ZERO);
                    b.setYear(obj.has("year") ? obj.get("year").getAsInt() : 0);
                    b.setPeriodType(obj.has("periodType") && !obj.get("periodType").isJsonNull() ? org.example.models.Budget.PeriodType.valueOf(obj.get("periodType").getAsString()) : null);
                    b.setMonth(obj.has("month") && !obj.get("month").isJsonNull() ? obj.get("month").getAsInt() : null);
                    b.setQuarter(obj.has("quarter") && !obj.get("quarter").isJsonNull() ? obj.get("quarter").getAsInt() : null);
                    list.add(b);
                }
            }
        } catch (Exception e) {
            System.err.println("ApiClient.getBudgets error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return list;
    }

    public static org.example.models.Budget postBudget(int userId, org.example.models.Budget budget) {
        HttpURLConnection conn = null;
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("category", budget.getCategory());
            payload.addProperty("limitAmount", budget.getLimitAmount());
            payload.addProperty("spentAmount", budget.getSpentAmount() != null ? budget.getSpentAmount() : 0);
            payload.addProperty("year", budget.getYear());
            payload.addProperty("periodType", budget.getPeriodType().name());
            if (budget.getMonth() != null) payload.addProperty("month", budget.getMonth());
            if (budget.getQuarter() != null) payload.addProperty("quarter", budget.getQuarter());

            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", userId);
            payload.add("user", userObj);

            conn = ApiUtil.fetchApi("/api/v1/budgets", ApiUtil.RequestMethod.POST, payload);
            if (conn != null && conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                JsonObject obj = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
                budget.setId(obj.get("id").getAsLong());
                return budget;
            }
        } catch (Exception e) {
            System.err.println("ApiClient.postBudget error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static boolean putBudget(int userId, org.example.models.Budget budget) {
        HttpURLConnection conn = null;
        try {
            JsonObject payload = new JsonObject();
            if (budget.getId() != null) payload.addProperty("id", budget.getId());
            payload.addProperty("category", budget.getCategory());
            payload.addProperty("limitAmount", budget.getLimitAmount());
            payload.addProperty("spentAmount", budget.getSpentAmount() != null ? budget.getSpentAmount() : 0);
            payload.addProperty("year", budget.getYear());
            payload.addProperty("periodType", budget.getPeriodType().name());
            if (budget.getMonth() != null) payload.addProperty("month", budget.getMonth());
            if (budget.getQuarter() != null) payload.addProperty("quarter", budget.getQuarter());

            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", userId);
            payload.add("user", userObj);

            conn = ApiUtil.fetchApi("/api/v1/budgets", ApiUtil.RequestMethod.PUT, payload);
            return conn != null && conn.getResponseCode() >= 200 && conn.getResponseCode() < 300;
        } catch (Exception e) {
            System.err.println("ApiClient.putBudget error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    public static boolean deleteBudget(long budgetId) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi("/api/v1/budgets/" + budgetId, ApiUtil.RequestMethod.DELETE, null);
            return conn != null && conn.getResponseCode() >= 200 && conn.getResponseCode() < 300;
        } catch (Exception e) {
            System.err.println("ApiClient.deleteBudget error: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }
}
