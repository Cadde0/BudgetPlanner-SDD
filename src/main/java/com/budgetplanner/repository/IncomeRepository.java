package com.budgetplanner.repository;

import com.budgetplanner.model.Income;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.GeneratedKeyHolder;
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

    public Income save(Income income) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO income (amount) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, income.getAmount());
            return ps;
        }, keyHolder);

        Number generatedId;
        Map<String, Object> generatedKeys = keyHolder.getKeys();
        if (generatedKeys != null && generatedKeys.get("id") instanceof Number idValue) {
            generatedId = idValue;
        } else {
            generatedId = keyHolder.getKey();
        }
        int id = generatedId == null ? 0 : generatedId.intValue();
        return new Income(id, income.getAmount());
    }

    public Optional<Income> update(int id, Income income) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE income SET amount = ? WHERE id = ?",
                income.getAmount(),
                id);

        if (updatedRows == 0) {
            return Optional.empty();
        }

        return Optional.of(new Income(id, income.getAmount()));
    }

    public boolean deleteById(int id) {
        return jdbcTemplate.update("DELETE FROM income WHERE id = ?", id) > 0;
    }
}
