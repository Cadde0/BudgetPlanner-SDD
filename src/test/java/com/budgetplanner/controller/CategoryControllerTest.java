package com.budgetplanner.controller;

import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import com.budgetplanner.repository.CategoryRepository;
import com.budgetplanner.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.*;

@WebMvcTest(CategoryController.class)
@ContextConfiguration(classes = { CategoryController.class })
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;
    @MockBean
    private ExpenseRepository expenseRepository;

    @Test
    void testGetCategorySummaries() throws Exception {
        Category food = new Category(1, "Food", 500, "Groceries and dining");
        Category travel = new Category(2, "Travel", 300, "Transport");
        List<Category> categories = Arrays.asList(food, travel);
        List<Expense> expenses = Arrays.asList(
                new Expense(1, 100, 1, "Lunch"),
                new Expense(2, 50, 1, "Groceries"),
                new Expense(3, 70, 2, "Bus ticket"));
        Mockito.when(categoryRepository.findAll()).thenReturn(categories);
        Mockito.when(expenseRepository.findAll()).thenReturn(expenses);

        mockMvc.perform(get("/categories/summaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].categoryId", is(1)))
                .andExpect(jsonPath("$[0].categoryName", is("Food")))
                .andExpect(jsonPath("$[0].totalExpense", is(150)))
                .andExpect(jsonPath("$[1].categoryId", is(2)))
                .andExpect(jsonPath("$[1].categoryName", is("Travel")))
                .andExpect(jsonPath("$[1].totalExpense", is(70)));
    }

            @Test
            void createCategoryReturnsCreated() throws Exception {
            Category created = new Category(7, "Utilities", 1200, "Monthly bills");
            Mockito.when(categoryRepository.save(any(Category.class))).thenReturn(created);

            mockMvc.perform(post("/categories")
                    .contentType("application/json")
                    .content("""
                        {
                          "name": "Utilities",
                          "categoryLimit": 1200,
                          "description": "Monthly bills"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/categories/7")))
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.name", is("Utilities")));
            }

            @Test
            void updateCategoryReturnsOkWhenCategoryExists() throws Exception {
            Category updated = new Category(3, "Updated", 900, "updated description");
            Mockito.when(categoryRepository.update(Mockito.eq(3), any(Category.class)))
                .thenReturn(Optional.of(updated));

            mockMvc.perform(put("/categories/3")
                    .contentType("application/json")
                    .content("""
                        {
                          "name": "Updated",
                          "categoryLimit": 900,
                          "description": "updated description"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Updated")));
            }

            @Test
            void updateCategoryReturnsNotFoundWhenMissing() throws Exception {
            Mockito.when(categoryRepository.update(Mockito.eq(999), any(Category.class)))
                .thenReturn(Optional.empty());

            mockMvc.perform(put("/categories/999")
                    .contentType("application/json")
                    .content("""
                        {
                          "name": "Missing",
                          "categoryLimit": 300,
                          "description": "missing"
                        }
                        """))
                .andExpect(status().isNotFound());
            }

            @Test
            void deleteCategoryReturnsNoContentWhenCategoryExists() throws Exception {
            Mockito.when(categoryRepository.deleteById(4)).thenReturn(true);

            mockMvc.perform(delete("/categories/4"))
                .andExpect(status().isNoContent());
            }

            @Test
            void deleteCategoryReturnsNotFoundWhenMissing() throws Exception {
            Mockito.when(categoryRepository.deleteById(404)).thenReturn(false);

            mockMvc.perform(delete("/categories/404"))
                .andExpect(status().isNotFound());
            }
}
