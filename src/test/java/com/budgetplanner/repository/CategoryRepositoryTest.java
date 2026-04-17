package com.budgetplanner.repository;

import com.budgetplanner.model.Category;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    private static final int KNOWN_CATEGORY_ID = 1;
    private static final String EXPECTED_CATEGORY_NAME = "Home";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository = new CategoryRepository(jdbcTemplate);
    }

    @Test
    void findAllContainsConfiguredCategoryFixture() {
        assumeTrue(KNOWN_CATEGORY_ID > 0, "Set KNOWN_CATEGORY_ID to an existing row in your database");

        List<Category> result = categoryRepository.findAll();

        assertTrue(result.stream().anyMatch(category -> KNOWN_CATEGORY_ID == category.getId()));
    }

    @Test
    void findByIdReturnsConfiguredCategoryFixture() {
        assumeTrue(KNOWN_CATEGORY_ID > 0, "Set KNOWN_CATEGORY_ID to an existing row in your database");
        assumeTrue(!EXPECTED_CATEGORY_NAME.isBlank(), "Set EXPECTED_CATEGORY_NAME for that fixture row");

        var result = categoryRepository.findById(KNOWN_CATEGORY_ID);

        assertTrue(result.isPresent());
        assertTrue(EXPECTED_CATEGORY_NAME.equals(result.get().getName()));
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        var result = categoryRepository.findById(Integer.MAX_VALUE);

        assertFalse(result.isPresent());
    }
}
