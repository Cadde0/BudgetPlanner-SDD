package com.budgetplanner.controller;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryUiAssetsTest {

    @Test
    void indexHtmlContainsCategoryEditDeleteUiElements() throws IOException {
        String html = readResource("/static/index.html");

        assertTrue(html.contains("id=\"categoryForm\""));
        assertTrue(html.contains("id=\"categoryTableBody\""));
        assertTrue(html.contains("id=\"categorySubmitButton\""));
        assertTrue(html.contains("id=\"categoryCancelButton\""));
    }

    @Test
    void appJsContainsCategoryEditDeleteHandlers() throws IOException {
        String js = readResource("/static/app.js");

        assertTrue(js.contains("handleCategorySubmit"));
        assertTrue(js.contains("handleCategoryTableClick"));
        assertTrue(js.contains("edit-category"));
        assertTrue(js.contains("delete-category"));
    }

    @Test
    void appJsContainsPerCategorySummaryRendering() throws IOException {
        String js = readResource("/static/app.js");

        assertTrue(js.contains("category-summary-strip"));
        assertTrue(js.contains("category-meter"));
        assertTrue(js.contains("of spend"));
    }

    @Test
    void indexHtmlContainsCategoryExpensesGridElements() throws IOException {
        String html = readResource("/static/index.html");

        assertTrue(html.contains("id=\"categoryExpensePanels\""));
        assertTrue(html.contains("class=\"category-expense-panels\""));
    }

    @Test
    void appJsContainsCategoryExpensesRendering() throws IOException {
        String js = readResource("/static/app.js");

        assertTrue(js.contains("renderExpenseTable"));
        assertTrue(js.contains("class=\"category-expense-panel\""));
        assertTrue(js.contains("class=\"category-summary-strip\""));
        assertTrue(js.contains("categoryExpensePanels"));
    }

    @Test
    void stylesCssContainsCategoryGridStyles() throws IOException {
        String css = readResource("/static/styles.css");

        assertTrue(css.contains(".category-expense-panels"));
        assertTrue(css.contains(".category-expense-panel"));
        assertTrue(css.contains("grid-template-columns"));
        assertTrue(css.contains(".category-summary-strip"));
    }

    private String readResource(String path) throws IOException {
        try (var stream = CategoryUiAssetsTest.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
