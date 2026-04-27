package com.budgetplanner.repository;

import com.budgetplanner.model.Category;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Provides JDBC access to category records.
 */
@Repository
public class CategoryRepository {

    private static final RowMapper<Category> CATEGORY_ROW_MAPPER = (rs, rowNum) -> new Category(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("category_limit"),
            rs.getString("description"));

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a category repository backed by the supplied JDBC template.
     *
     * @param jdbcTemplate the JDBC template to use
     */
    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns all categories ordered by identifier.
     *
     * @return all categories
     */
    public List<Category> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name, category_limit, description FROM category ORDER BY id",
                CATEGORY_ROW_MAPPER);
    }

    /**
     * Returns the category for the supplied identifier.
     *
     * @param id the category identifier
     * @return the matching category, if any
     */
    public Optional<Category> findById(int id) {
        List<Category> results = jdbcTemplate.query(
                "SELECT id, name, category_limit, description FROM category WHERE id = ?",
                CATEGORY_ROW_MAPPER,
                id);
        return results.stream().findFirst();
    }

    /**
     * Stores a new category and returns the persisted record.
     *
     * @param category the category to persist
     * @return the persisted category
     */
    public Category save(Category category) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO category (name, category_limit, description) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName());
            ps.setObject(2, category.getCategoryLimit());
            ps.setString(3, category.getDescription());
            return ps;
        }, keyHolder);

        Number generatedId = resolveGeneratedId(keyHolder);

        int id = generatedId == null ? 0 : generatedId.intValue();
        return new Category(id, category.getName(), category.getCategoryLimit(), category.getDescription());
    }

    private Number resolveGeneratedId(KeyHolder keyHolder) {
        Map<String, Object> generatedKeys = keyHolder.getKeys();
        if (generatedKeys == null || generatedKeys.isEmpty()) {
            return keyHolder.getKey();
        }

        Number idFromIdKey = findNumberByKeyIgnoreCase(generatedKeys, "id");
        if (idFromIdKey != null) {
            return idFromIdKey;
        }

        Number firstNumber = findFirstNumberValue(generatedKeys);
        if (firstNumber != null) {
            return firstNumber;
        }

        return keyHolder.getKey();
    }

    private Number findNumberByKeyIgnoreCase(Map<String, Object> values, String keyName) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (keyName.equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number numberValue) {
                return numberValue;
            }
        }

        return null;
    }

    private Number findFirstNumberValue(Map<String, Object> values) {
        for (Object value : values.values()) {
            if (value instanceof Number numberValue) {
                return numberValue;
            }
        }

        return null;
    }

    /**
     * Updates an existing category.
     *
     * @param id the category identifier
     * @param category the updated category data
     * @return the updated category, if found
     */
    public Optional<Category> update(int id, Category category) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE category SET name = ?, category_limit = ?, description = ? WHERE id = ?",
                category.getName(),
                category.getCategoryLimit(),
                category.getDescription(),
                id);

        if (updatedRows == 0) {
            return Optional.empty();
        }

        return findById(id);
    }

    /**
     * Deletes the category with the supplied identifier.
     *
     * @param id the category identifier
     * @return {@code true} when a category was deleted
     */
    public boolean deleteById(int id) {
        return jdbcTemplate.update("DELETE FROM category WHERE id = ?", id) > 0;
    }
}
