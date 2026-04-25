package com.budgetplanner.repository;

import com.budgetplanner.model.Expense;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpenseRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        expenseRepository = new ExpenseRepository(jdbcTemplate);
    }

    @Test
    void findAllContainsSavedExpense() {
        Integer existingCategoryId = findAnyCategoryId();
        assumeTrue(existingCategoryId != null, "Test requires at least one existing category row");

        Expense created = expenseRepository.save(new Expense(null, 741, existingCategoryId, "findAll fixture"));

        try {
            List<Expense> result = expenseRepository.findAll();

            assertTrue(result.stream().anyMatch(expense -> created.getId().equals(expense.getId())));
        } finally {
            jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", created.getId());
        }
    }

    @Test
    void findByIdReturnsSavedExpenseFixture() {
        Integer existingCategoryId = findAnyCategoryId();
        assumeTrue(existingCategoryId != null, "Test requires at least one existing category row");

        Expense created = expenseRepository.save(new Expense(null, 987, existingCategoryId, "findById fixture"));

        try {
            var result = expenseRepository.findById(created.getId());

            assertTrue(result.isPresent());
            assertEquals(987, result.get().getAmount());
            assertEquals(existingCategoryId, result.get().getCategoryId());
            assertEquals("findById fixture", result.get().getDescription());
        } finally {
            jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", created.getId());
        }
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        var result = expenseRepository.findById(Integer.MAX_VALUE);

        assertFalse(result.isPresent());
    }

    @Test
    void savePersistsExpenseAndReturnsGeneratedId() {
        Integer existingCategoryId = findAnyCategoryId();
        assumeTrue(existingCategoryId != null, "Test requires at least one existing category row");

        Expense toCreate = new Expense(null, 987, existingCategoryId, "created by T023 test");
        Expense saved = expenseRepository.save(toCreate);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals(987, saved.getAmount());
        assertEquals(existingCategoryId, saved.getCategoryId());
        assertEquals("created by T023 test", saved.getDescription());

        try {
            var fromDb = expenseRepository.findById(saved.getId());
            assertTrue(fromDb.isPresent());
            assertEquals(987, fromDb.get().getAmount());
            assertEquals(existingCategoryId, fromDb.get().getCategoryId());
            assertEquals("created by T023 test", fromDb.get().getDescription());
        } finally {
            jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", saved.getId());
        }
    }

    @Test
    void deleteByIdRemovesExpense() {
        Integer existingCategoryId = findAnyCategoryId();
        assumeTrue(existingCategoryId != null, "Test requires at least one existing category row");

        Expense created = expenseRepository.save(new Expense(null, 321, existingCategoryId, "delete by T029 test"));
        try {
            boolean deleted = expenseRepository.deleteById(created.getId());

            assertTrue(deleted);
            assertFalse(expenseRepository.findById(created.getId()).isPresent());
        } finally {
            jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", created.getId());
        }
    }

    @Test
    void sumAmountsByCategoryReturnsAggregatedTotals() {
        List<Integer> categoryIds = findCategoryIds(2);
        assumeTrue(categoryIds.size() >= 2, "Test requires at least two existing category rows");

        Integer firstCategoryId = categoryIds.get(0);
        Integer secondCategoryId = categoryIds.get(1);

        var baselineTotals = expenseRepository.sumAmountsByCategory();
        int baselineFirstCategory = baselineTotals.getOrDefault(firstCategoryId, 0);
        int baselineSecondCategory = baselineTotals.getOrDefault(secondCategoryId, 0);

        Expense first = expenseRepository.save(new Expense(null, 111, firstCategoryId, "summary fixture 1"));
        Expense second = expenseRepository.save(new Expense(null, 222, firstCategoryId, "summary fixture 2"));
        Expense third = expenseRepository.save(new Expense(null, 50, secondCategoryId, "summary fixture 3"));

        try {
            var totals = expenseRepository.sumAmountsByCategory();

            assertEquals(baselineFirstCategory + 333, totals.getOrDefault(firstCategoryId, 0));
            assertEquals(baselineSecondCategory + 50, totals.getOrDefault(secondCategoryId, 0));
        } finally {
            jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", first.getId());
            jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", second.getId());
            jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", third.getId());
        }
    }

    private Integer findAnyCategoryId() {
        List<Integer> ids = jdbcTemplate.query("SELECT id FROM category ORDER BY id LIMIT 1", (rs, rowNum) -> rs.getInt("id"));
        return ids.isEmpty() ? null : ids.get(0);
    }

    private List<Integer> findCategoryIds(int limit) {
        return jdbcTemplate.query(
                "SELECT id FROM category ORDER BY id LIMIT ?",
                (rs, rowNum) -> rs.getInt("id"),
                limit);
    }
}
