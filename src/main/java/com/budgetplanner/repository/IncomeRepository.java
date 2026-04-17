package com.budgetplanner.repository;

import com.budgetplanner.model.Income;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class IncomeRepository {

    private static final RowMapper<Income> INCOME_ROW_MAPPER = (rs, rowNum) -> new Income(rs.getInt("id"),
            rs.getInt("amount"));

    private final JdbcTemplate jdbcTemplate;

    public IncomeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Income> findAll() {
        return jdbcTemplate.query("SELECT id, amount FROM income ORDER BY id", INCOME_ROW_MAPPER);
    }

    public Optional<Income> findById(int id) {
        List<Income> results = jdbcTemplate.query(
                "SELECT id, amount FROM income WHERE id = ?",
                INCOME_ROW_MAPPER,
                id);
        return results.stream().findFirst();
    }
}
