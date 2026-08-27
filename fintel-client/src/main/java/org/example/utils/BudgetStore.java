
package org.example.utils;

import org.example.models.Budget;
import java.util.*;

public final class BudgetStore {

    private BudgetStore(){}

    private static volatile List<Budget> cachedBudgets;
    private static volatile int cachedUserId = -1;
    private static volatile long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 2000;

    public static List<Budget> getBudgets(int userId) {
        long now = System.currentTimeMillis();
        if (userId == cachedUserId && cachedBudgets != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            return new ArrayList<>(cachedBudgets);
        }
        List<Budget> result = ApiClient.getBudgets(userId);
        cachedBudgets = result;
        cachedUserId = userId;
        cacheTimestamp = now;
        return new ArrayList<>(result);
    }

    public static void invalidateCache() {
        cachedBudgets = null;
        cachedUserId = -1;
        cacheTimestamp = 0;
    }

    public static Budget getById(int userId, Long id) {
        if (id == null) return null;
        List<Budget> all = getBudgets(userId);
        for (Budget b : all) {
            if (id.equals(b.getId())) return b;
        }
        return null;
    }

    public static Budget add(int userId, Budget b) {
        Budget result = ApiClient.postBudget(userId, b);
        invalidateCache();
        return result;
    }

    public static boolean update(int userId, Budget b) {
        boolean result = ApiClient.putBudget(userId, b);
        invalidateCache();
        return result;
    }

    public static boolean delete(long budgetId) {
        boolean result = ApiClient.deleteBudget(budgetId);
        invalidateCache();
        return result;
    }

    public static boolean removeById(int userId, Long id) {
        if (id == null) return false;
        boolean result = ApiClient.deleteBudget(id);
        invalidateCache();
        return result;
    }
}
