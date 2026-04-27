package com.budgetplanner.repository;

import com.budgetplanner.model.Category;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository = new CategoryRepository(jdbcTemplate);
    }

    @Test
    void findAllReturnsCategories() {
        Category seed = categoryRepository.save(new Category(null, "Test Find All " + System.nanoTime(), 1000, "test"));

        try {
            List<Category> result = categoryRepository.findAll();

            assertTrue(!result.isEmpty());
            assertTrue(result.stream().anyMatch(category -> seed.getId().equals(category.getId())));
        } finally {
            categoryRepository.deleteById(seed.getId());
        }
    }

    @Test
    void findByIdReturnsPersistedCategory() {
        Category seed = categoryRepository.save(new Category(null, "Test Find By Id " + System.nanoTime(), 1500, "test"));

        try {
            var result = categoryRepository.findById(seed.getId());

            assertTrue(result.isPresent());
            assertEquals(seed.getName(), result.get().getName());
        } finally {
            categoryRepository.deleteById(seed.getId());
        }
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        var result = categoryRepository.findById(Integer.MAX_VALUE);

        assertFalse(result.isPresent());
    }

    @Test
    void savePersistsCategoryAndReturnsGeneratedId() {
        Category category = new Category(null, "T019 Test Category", 1500, "created by test");

        Category saved = categoryRepository.save(category);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals("T019 Test Category", saved.getName());
        assertEquals(1500, saved.getCategoryLimit());
        assertEquals("created by test", saved.getDescription());

        try {
            Optional<Category> fromDb = categoryRepository.findById(saved.getId());
            assertTrue(fromDb.isPresent());
            assertEquals("T019 Test Category", fromDb.get().getName());
        } finally {
            categoryRepository.deleteById(saved.getId());
        }
    }

    @Test
    void updateChangesStoredCategoryValues() {
        Category seed = categoryRepository.save(new Category(null, "T019 Update Seed", 1200, "seed"));

        try {
            Category updatedValues = new Category(null, "T019 Update Changed", 3300, "updated");
            Optional<Category> updated = categoryRepository.update(seed.getId(), updatedValues);

            assertTrue(updated.isPresent());
            assertEquals("T019 Update Changed", updated.get().getName());
            assertEquals(3300, updated.get().getCategoryLimit());
            assertEquals("updated", updated.get().getDescription());
        } finally {
            categoryRepository.deleteById(seed.getId());
        }
    }

    @Test
    void deleteByIdRemovesStoredCategory() {
        Category seed = categoryRepository.save(new Category(null, "T019 Delete Seed", 700, "delete"));

        boolean deleted = categoryRepository.deleteById(seed.getId());

        assertTrue(deleted);
        assertFalse(categoryRepository.findById(seed.getId()).isPresent());
    }
}
