package com.budgetplanner.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetUiAssetsTest {

    @Test
    void indexHtmlContainsLiveBudgetStatusElement() throws IOException {
        String html = readResource("/static/index.html");

        assertTrue(html.contains("id=\"budgetSyncStatus\""));
    }

    @Test
    void appJsContainsLiveBudgetPollingHooks() throws IOException {
        String js = readResource("/static/app.js");

        assertTrue(js.contains("/budget/remaining"));
        assertTrue(js.contains("loadBudgetSnapshot"));
        assertTrue(js.contains("setInterval"));
        assertTrue(js.contains("budgetSyncStatus"));
    }

    private String readResource(String path) throws IOException {
        try (var stream = BudgetUiAssetsTest.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}