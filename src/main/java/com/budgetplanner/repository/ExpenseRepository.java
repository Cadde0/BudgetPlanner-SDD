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


    public Optional<Expense> updateCategory(int id, int categoryId) {
        int updated = jdbcTemplate.update(
                "UPDATE expenses SET category_id = ? WHERE id = ?",
                categoryId,
                id
        );
        if (updated > 0) {
            return findById(id);
        } else {
            return Optional.empty();
        }
    }

}
  