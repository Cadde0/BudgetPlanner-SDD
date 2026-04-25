package com.budgetplanner.controller;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseUiAssetsTest {

    @Test
    void indexHtmlContainsExpenseUiElements() throws IOException {
        String html = readResource("/static/index.html");

        assertTrue(html.contains("id=\"expenseForm\""));
        assertTrue(html.contains("id=\"categoryExpensePanels\""));
        assertTrue(html.contains("id=\"expenseSubmitButton\""));
        assertTrue(html.contains("id=\"expenseCancelButton\""));
    }

    @Test
    void appJsContainsExpenseDeleteHandlers() throws IOException {
        String js = readResource("/static/app.js");

        assertTrue(js.contains("assign-category"));
        assertTrue(js.contains("Please select a valid category before assigning."));
        assertTrue(js.contains("/expenses/${expenseId}/category"));
        assertTrue(js.contains("Category assigned successfully."));
        assertTrue(js.contains("delete-expense"));
        assertTrue(js.contains("Delete expense #${expenseId}?"));
        assertTrue(js.contains("/expenses/${expenseId}"));
        assertTrue(js.contains("Expense deleted successfully."));
    }

    private String readResource(String path) throws IOException {
        try (var stream = ExpenseUiAssetsTest.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
