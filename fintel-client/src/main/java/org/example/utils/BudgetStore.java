package org.example.utils;

import org.example.models.Budget;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BudgetStore {

    private BudgetStore(){}

    private static volatile List<Budget> cachedBudgets;
    private static volatile int cachedUserId = -1;
    private static volatile long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 2000;

    private static final Map<Integer, List<Budget>> localStore = new ConcurrentHashMap<>();
    private static long localIdCounter = 1000L;

    public static List<Budget> getBudgets(int userId) {
        long now = System.currentTimeMillis();
        if (userId == cachedUserId && cachedBudgets != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            return new ArrayList<>(cachedBudgets);
        }
        List<Budget> result = ApiClient.getBudgets(userId);
        if (result == null || result.isEmpty()) {
            List<Budget> local = localStore.get(userId);
            if (local != null && !local.isEmpty()) {
                result = new ArrayList<>(local);
            } else if (result == null) {
                result = new ArrayList<>();
            }
        }
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
        if (result == null) {
            // Local fallback for offline mode / unit testing
            if (b.getId() == null) {
                b.setId(localIdCounter++);
            }
            localStore.computeIfAbsent(userId, k -> new ArrayList<>()).add(b);
            result = b;
        }
        invalidateCache();
        return result;
    }

    public static boolean update(int userId, Budget b) {
        boolean result = ApiClient.putBudget(userId, b);
        List<Budget> local = localStore.get(userId);
        if (local != null) {
            for (int i = 0; i < local.size(); i++) {
                if (Objects.equals(local.get(i).getId(), b.getId())) {
                    local.set(i, b);
                    break;
                }
            }
        }
        invalidateCache();
        return result;
    }

    public static boolean delete(long budgetId) {
        boolean result = ApiClient.deleteBudget(budgetId);
        for (List<Budget> list : localStore.values()) {
            list.removeIf(b -> Objects.equals(b.getId(), budgetId));
        }
        invalidateCache();
        return result;
    }

    public static boolean removeById(int userId, Long id) {
        if (id == null) return false;
        boolean result = ApiClient.deleteBudget(id);
        List<Budget> local = localStore.get(userId);
        if (local != null) {
            local.removeIf(b -> Objects.equals(b.getId(), id));
        }
        invalidateCache();
        return result;
    }
}
