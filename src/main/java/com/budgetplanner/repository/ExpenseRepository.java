package com.budgetplanner.repository;

import com.budgetplanner.model.Expense;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

    public Optional<Expense> findById(int id) {
        List<Expense> results = jdbcTemplate.query(
                "SELECT id, amount, category_id, description FROM expenses WHERE id = ?",
                EXPENSE_ROW_MAPPER,
                id);
        return results.stream().findFirst();
    }

    public Expense save(Expense expense) {
        jdbcTemplate.update(
                "INSERT INTO expenses (amount, category_id, description) VALUES (?, ?, ?)",
                expense.getAmount(),
                expense.getCategoryId(),
                expense.getDescription()
        );
        // Retrieve the last inserted expense (assuming id is auto-increment)
        return jdbcTemplate.query(
                "SELECT id, amount, category_id, description FROM expenses ORDER BY id DESC LIMIT 1",
                EXPENSE_ROW_MAPPER
        ).get(0);
    }

    public Optional<Expense> update(int id, Expense expense) {
        int updated = jdbcTemplate.update(
                "UPDATE expenses SET amount = ?, category_id = ?, description = ? WHERE id = ?",
                expense.getAmount(),
                expense.getCategoryId(),
                expense.getDescription(),
                id
        );
        if (updated > 0) {
            return findById(id);
        } else {
            return Optional.empty();
        }
    }

    public boolean deleteById(int id) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM expenses WHERE id = ?",
                id
        );
        return deleted > 0;
    }
}
  