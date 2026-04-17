package com.budgetplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CategoryTest {

    @Test
    void defaultConstructorInitializesFieldsAsNull() {
        Category category = new Category();

        assertNull(category.getId());
        assertNull(category.getName());
        assertNull(category.getCategoryLimit());
        assertNull(category.getDescription());
    }

    @Test
    void allArgsConstructorSetsAllFields() {
        Category category = new Category(2, "Food", 5000, "Monthly food budget");

        assertEquals(2, category.getId());
        assertEquals("Food", category.getName());
        assertEquals(5000, category.getCategoryLimit());
        assertEquals("Monthly food budget", category.getDescription());
    }

    @Test
    void settersUpdateFields() {
        Category category = new Category();

        category.setId(4);
        category.setName("Transport");
        category.setCategoryLimit(2000);
        category.setDescription("Bus and train");

        assertEquals(4, category.getId());
        assertEquals("Transport", category.getName());
        assertEquals(2000, category.getCategoryLimit());
        assertEquals("Bus and train", category.getDescription());
    }
}
