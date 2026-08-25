package org.example;

import org.example.models.Budget;
import org.example.utils.BudgetStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetStoreTest {

    private final int testUserId = 9999;

    @BeforeEach
    void setUp() {
        List<Budget> existing = BudgetStore.getBudgets(testUserId);
        for (Budget b : existing) {
            BudgetStore.removeById(testUserId, b.getId());
        }
    }

    @Test
    void testAddAndGetBudgets() {
        Budget b = new Budget();
        b.setUserEmail("test@example.com");
        b.setCategory("Food");
        b.setLimitAmount(BigDecimal.valueOf(500));
        b.setYear(2026);
        b.setPeriodType(Budget.PeriodType.MONTHLY);
        b.setMonth(8);

        BudgetStore.add(testUserId, b);

        List<Budget> list = BudgetStore.getBudgets(testUserId);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Food", list.get(0).getCategory());
        assertNotNull(list.get(0).getId());
    }

    @Test
    void testGetById() {
        Budget b = new Budget();
        b.setUserEmail("test@example.com");
        b.setCategory("Travel");
        b.setLimitAmount(BigDecimal.valueOf(1000));
        b.setYear(2026);
        b.setPeriodType(Budget.PeriodType.MONTHLY);
        b.setMonth(8);

        BudgetStore.add(testUserId, b);
        Long id = b.getId();
        assertNotNull(id);

        Budget retrieved = BudgetStore.getById(testUserId, id);
        assertNotNull(retrieved);
        assertEquals("Travel", retrieved.getCategory());
    }

    @Test
    void testRemoveById() {
        Budget b = new Budget();
        b.setUserEmail("test@example.com");
        b.setCategory("Utilities");
        b.setLimitAmount(BigDecimal.valueOf(300));
        b.setYear(2026);
        b.setPeriodType(Budget.PeriodType.MONTHLY);
        b.setMonth(8);

        BudgetStore.add(testUserId, b);
        Long id = b.getId();

        BudgetStore.removeById(testUserId, id);
        assertNull(BudgetStore.getById(testUserId, id));
    }
}
