package com.budgetplanner.repository;

import com.budgetplanner.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {

    private static final RowMapper<Category> CATEGORY_ROW_MAPPER = (rs, rowNum) -> new Category(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("category_limit"),
            rs.getString("description"));

    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name, category_limit, description FROM category ORDER BY id",
                CATEGORY_ROW_MAPPER);
    }

    public Optional<Category> findById(int id) {
        List<Category> results = jdbcTemplate.query(
                "SELECT id, name, category_limit, description FROM category WHERE id = ?",
                CATEGORY_ROW_MAPPER,
                id);
        return results.stream().findFirst();
    }
}
