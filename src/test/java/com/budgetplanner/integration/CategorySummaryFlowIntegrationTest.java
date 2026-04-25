package com.budgetplanner.integration;

import com.budgetplanner.controller.CategoryController;
import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.budgetplanner.BudgetPlannerApplication.class)
class CategorySummaryFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void categorySummariesAndCategoryExpenseListingMatchUnderlyingExpenses() {
        String categoriesUrl = "http://localhost:" + port + "/categories";
        String expensesUrl = "http://localhost:" + port + "/expenses";

        Integer createdCategoryOneId = null;
        Integer createdCategoryTwoId = null;
        Integer createdExpenseOneId = null;
        Integer createdExpenseTwoId = null;

        int firstCategoryFirstExpenseAmount = 120;
        int firstCategorySecondExpenseAmount = 80;
        int secondCategoryExpenseAmount = 45;

        try {
            ResponseEntity<Category> createCategoryOneResponse = restTemplate.postForEntity(
                    categoriesUrl,
                    Map.of(
                            "name", "T039 Category One " + System.nanoTime(),
                            "description", "first category for FR-010 integration test"),
                    Category.class);
            assertThat(createCategoryOneResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createCategoryOneResponse.getBody()).isNotNull();
            createdCategoryOneId = createCategoryOneResponse.getBody().getId();
            assertThat(createdCategoryOneId).isNotNull();

            ResponseEntity<Category> createCategoryTwoResponse = restTemplate.postForEntity(
                    categoriesUrl,
                    Map.of(
                            "name", "T039 Category Two " + System.nanoTime(),
                            "description", "second category for FR-010 integration test"),
                    Category.class);
            assertThat(createCategoryTwoResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createCategoryTwoResponse.getBody()).isNotNull();
            createdCategoryTwoId = createCategoryTwoResponse.getBody().getId();
            assertThat(createdCategoryTwoId).isNotNull();

            ResponseEntity<Expense> createExpenseOneResponse = restTemplate.postForEntity(
                    expensesUrl,
                    Map.of(
                            "amount", firstCategoryFirstExpenseAmount,
                            "categoryId", createdCategoryOneId,
                            "description", "T039 category one expense A"),
                    Expense.class);
            assertThat(createExpenseOneResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createExpenseOneResponse.getBody()).isNotNull();
            createdExpenseOneId = createExpenseOneResponse.getBody().getId();

            ResponseEntity<Expense> createExpenseTwoResponse = restTemplate.postForEntity(
                    expensesUrl,
                    Map.of(
                            "amount", firstCategorySecondExpenseAmount,
                            "categoryId", createdCategoryOneId,
                            "description", "T039 category one expense B"),
                    Expense.class);
            assertThat(createExpenseTwoResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createExpenseTwoResponse.getBody()).isNotNull();
            createdExpenseTwoId = createExpenseTwoResponse.getBody().getId();

            ResponseEntity<Expense> createSecondCategoryExpenseResponse = restTemplate.postForEntity(
                    expensesUrl,
                    Map.of(
                            "amount", secondCategoryExpenseAmount,
                            "categoryId", createdCategoryTwoId,
                            "description", "T039 category two expense"),
                    Expense.class);
            assertThat(createSecondCategoryExpenseResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createSecondCategoryExpenseResponse.getBody()).isNotNull();

            ResponseEntity<CategoryController.CategorySummary[]> summariesResponse = restTemplate.getForEntity(
                    categoriesUrl + "/summaries",
                    CategoryController.CategorySummary[].class);
            assertThat(summariesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(summariesResponse.getBody()).isNotNull();

            CategoryController.CategorySummary[] summaries = summariesResponse.getBody();
            int expectedFirstCategoryTotal = firstCategoryFirstExpenseAmount + firstCategorySecondExpenseAmount;
            final Integer categoryOneId = createdCategoryOneId;
            final Integer categoryTwoId = createdCategoryTwoId;

            assertThat(summaries)
                    .anyMatch(summary -> categoryOneId.equals(summary.getCategoryId())
                            && Integer.valueOf(expectedFirstCategoryTotal).equals(summary.getTotalExpense()));
            assertThat(summaries)
                    .anyMatch(summary -> categoryTwoId.equals(summary.getCategoryId())
                            && Integer.valueOf(secondCategoryExpenseAmount).equals(summary.getTotalExpense()));

            ResponseEntity<Expense[]> firstCategoryExpensesResponse = restTemplate.getForEntity(
                    categoriesUrl + "/" + createdCategoryOneId + "/expenses",
                    Expense[].class);
            assertThat(firstCategoryExpensesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(firstCategoryExpensesResponse.getBody()).isNotNull();
            assertThat(firstCategoryExpensesResponse.getBody()).hasSize(2);
            assertThat(firstCategoryExpensesResponse.getBody())
                    .allMatch(expense -> categoryOneId.equals(expense.getCategoryId()));
            assertThat(firstCategoryExpensesResponse.getBody())
                    .extracting(Expense::getAmount)
                    .containsExactlyInAnyOrder(firstCategoryFirstExpenseAmount, firstCategorySecondExpenseAmount);

            ResponseEntity<Expense[]> secondCategoryExpensesResponse = restTemplate.getForEntity(
                    categoriesUrl + "/" + createdCategoryTwoId + "/expenses",
                    Expense[].class);
            assertThat(secondCategoryExpensesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(secondCategoryExpensesResponse.getBody()).isNotNull();
            assertThat(secondCategoryExpensesResponse.getBody()).hasSize(1);
            assertThat(secondCategoryExpensesResponse.getBody()[0].getCategoryId()).isEqualTo(createdCategoryTwoId);
            assertThat(secondCategoryExpensesResponse.getBody()[0].getAmount()).isEqualTo(secondCategoryExpenseAmount);
        } finally {
            if (createdExpenseOneId != null) {
                restTemplate.exchange(expensesUrl + "/" + createdExpenseOneId, HttpMethod.DELETE, null, Void.class);
            }
            if (createdExpenseTwoId != null) {
                restTemplate.exchange(expensesUrl + "/" + createdExpenseTwoId, HttpMethod.DELETE, null, Void.class);
            }

            // Delete any expense still linked to the temporary categories before removing categories.
            if (createdCategoryOneId != null) {
                ResponseEntity<Expense[]> categoryOneExpenses = restTemplate.getForEntity(
                        categoriesUrl + "/" + createdCategoryOneId + "/expenses",
                        Expense[].class);
                if (categoryOneExpenses.getBody() != null) {
                    for (Expense expense : categoryOneExpenses.getBody()) {
                        if (expense.getId() != null) {
                            restTemplate.exchange(expensesUrl + "/" + expense.getId(), HttpMethod.DELETE, null, Void.class);
                        }
                    }
                }
                restTemplate.exchange(categoriesUrl + "/" + createdCategoryOneId, HttpMethod.DELETE, null, Void.class);
            }
            if (createdCategoryTwoId != null) {
                ResponseEntity<Expense[]> categoryTwoExpenses = restTemplate.getForEntity(
                        categoriesUrl + "/" + createdCategoryTwoId + "/expenses",
                        Expense[].class);
                if (categoryTwoExpenses.getBody() != null) {
                    for (Expense expense : categoryTwoExpenses.getBody()) {
                        if (expense.getId() != null) {
                            restTemplate.exchange(expensesUrl + "/" + expense.getId(), HttpMethod.DELETE, null, Void.class);
                        }
                    }
                }
                restTemplate.exchange(categoriesUrl + "/" + createdCategoryTwoId, HttpMethod.DELETE, null, Void.class);
            }
        }
    }
}