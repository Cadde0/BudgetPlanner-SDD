package com.budgetplanner.repository;

import com.budgetplanner.model.Expense;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Provides JDBC access to expense records.
 */
@Repository
public class ExpenseRepository {

    private static final RowMapper<Expense> EXPENSE_ROW_MAPPER = (rs, rowNum) -> new Expense(
            rs.getInt("id"),
            rs.getInt("amount"),
            rs.getInt("category_id"),
            rs.getString("description"));

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates an expense repository backed by the supplied JDBC template.
     *
     * @param jdbcTemplate the JDBC template to use
     */
    public ExpenseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns all expenses ordered by identifier.
     *
     * @return all expenses
     */
    public List<Expense> findAll() {
        return jdbcTemplate.query(
                "SELECT id, amount, category_id, description FROM expenses ORDER BY id",
                EXPENSE_ROW_MAPPER);
    }

    /**
     * Returns the total expense amount grouped by category identifier.
     *
     * @return totals keyed by category identifier
     */
    public Map<Integer, Integer> sumAmountsByCategory() {
        return jdbcTemplate.query(
                "SELECT category_id, SUM(amount) AS total_amount "
                        + "FROM expenses "
                        + "WHERE category_id IS NOT NULL "
                        + "GROUP BY category_id",
                resultSet -> {
                    Map<Integer, Integer> totalsByCategory = new java.util.HashMap<>();
                    while (resultSet.next()) {
                        int categoryId = resultSet.getInt("category_id");
                        int totalAmount = resultSet.getInt("total_amount");
                        totalsByCategory.put(categoryId, totalAmount);
                    }
                    return totalsByCategory;
                });
    }

    /**
     * Returns the expense for the supplied identifier.
     *
     * @param id the expense identifier
     * @return the matching expense, if any
     */
    public Optional<Expense> findById(int id) {
        List<Expense> results = jdbcTemplate.query(
                "SELECT id, amount, category_id, description FROM expenses WHERE id = ?",
                EXPENSE_ROW_MAPPER,
                id);
        return results.stream().findFirst();
    }

    /**
     * Stores a new expense and returns the persisted record.
     *
     * @param expense the expense to persist
     * @return the persisted expense
     */
    public Expense save(Expense expense) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO expenses (amount, category_id, description) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, expense.getAmount());
            ps.setObject(2, expense.getCategoryId());
            ps.setString(3, expense.getDescription());
            return ps;
        }, keyHolder);

        Number generatedId = resolveGeneratedId(keyHolder);
        int id = generatedId == null ? 0 : generatedId.intValue();
        return new Expense(id, expense.getAmount(), expense.getCategoryId(), expense.getDescription());
    }

    /**
     * Resolves the generated key from the insert operation.
     *
     * @param keyHolder the key holder populated by the insert
     * @return the generated identifier, or {@code null} when unavailable
     */
    private Number resolveGeneratedId(KeyHolder keyHolder) {
        Map<String, Object> generatedKeys = keyHolder.getKeys();
        if (generatedKeys == null || generatedKeys.isEmpty()) {
            return keyHolder.getKey();
        }

        for (Map.Entry<String, Object> entry : generatedKeys.entrySet()) {
            if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number numberValue) {
                return numberValue;
            }
        }

        for (Object value : generatedKeys.values()) {
            if (value instanceof Number numberValue) {
                return numberValue;
            }
        }

        return keyHolder.getKey();
    }

    /**
     * Updates only the category assignment for the supplied expense.
     *
     * @param id the expense identifier
     * @param categoryId the category identifier to assign
     * @return the updated expense, if found
     */
    public Optional<Expense> updateCategory(int id, int categoryId) {
        int updated = jdbcTemplate.update(
                "UPDATE expenses SET category_id = ? WHERE id = ?",
                categoryId,
                id);
        if (updated > 0) {
            return findById(id);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Updates an existing expense.
     *
     * @param id the expense identifier
     * @param expense the updated expense data
     * @return the updated expense, if found
     */
    public Optional<Expense> updateExpense(int id, Expense expense) {
        int updated = jdbcTemplate.update(
                "UPDATE expenses SET amount = ?, category_id = ?, description = ? WHERE id = ?",
                expense.getAmount(),
                expense.getCategoryId(),
                expense.getDescription(),
                id);
        if (updated > 0) {
            return findById(id);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Deletes the expense with the supplied identifier.
     *
     * @param id the expense identifier
     * @return {@code true} when an expense was deleted
     */
    public boolean deleteById(int id) {
        return jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", id) > 0;
    }

    /**
     * Find all expenses for a given category.
     * @param categoryId the category id
     * @return list of expenses for the given category
     */
    public List<Expense> findByCategory(int categoryId) {
        return jdbcTemplate.query(
                "SELECT id, amount, category_id, description FROM expenses WHERE category_id = ? ORDER BY id",
                EXPENSE_ROW_MAPPER,
                categoryId);
    }

}
