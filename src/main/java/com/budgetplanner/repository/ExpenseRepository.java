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

@Repository
public class ExpenseRepository {

    private static final RowMapper<Expense> EXPENSE_ROW_MAPPER = (rs, rowNum) -> new Expense(
            rs.getInt("id"),
            rs.getInt("amount"),
            rs.getInt("category_id"),
            rs.getString("description"));

    private final JdbcTemplate jdbcTemplate;

    public ExpenseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Expense> findAll() {
        return jdbcTemplate.query(
                "SELECT id, amount, category_id, description FROM expenses ORDER BY id",
                EXPENSE_ROW_MAPPER);
    }

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

    public Optional<Expense> findById(int id) {
        List<Expense> results = jdbcTemplate.query(
                "SELECT id, amount, category_id, description FROM expenses WHERE id = ?",
                EXPENSE_ROW_MAPPER,
                id);
        return results.stream().findFirst();
    }

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

    public boolean deleteById(int id) {
        return jdbcTemplate.update("DELETE FROM expenses WHERE id = ?", id) > 0;
    }

}
