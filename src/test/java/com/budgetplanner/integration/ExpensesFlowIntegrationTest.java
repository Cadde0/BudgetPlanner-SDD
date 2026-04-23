package com.budgetplanner.integration;

import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import com.budgetplanner.repository.CategoryRepository;
import com.budgetplanner.repository.ExpenseRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.budgetplanner.BudgetPlannerApplication.class)
class ExpensesFlowIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ExpenseRepository expenseRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void assignCategoryUpdatesExpenseCategory() {
		List<Expense> expenses = expenseRepository.findAll();
		List<Category> categories = categoryRepository.findAll();

		assumeTrue(!expenses.isEmpty(), "Test requires at least one existing expense row");
		assumeTrue(!categories.isEmpty(), "Test requires at least one existing category row");

		Expense expense = expenses.get(0);
		Integer originalCategoryId = expense.getCategoryId();

		Integer targetCategoryId = categories.stream()
				.map(Category::getId)
				.filter(id -> id != null && id > 0)
				.filter(id -> !id.equals(originalCategoryId))
				.findFirst()
				.orElse(null);

		assumeTrue(targetCategoryId != null,
				"Test requires a category id different from the expense's current category");

		String url = "http://localhost:" + port + "/expenses/" + expense.getId() + "/category";

		try {
			ResponseEntity<Expense> updateResponse = restTemplate.exchange(
					url,
					HttpMethod.PUT,
					new HttpEntity<>(Map.of("categoryId", targetCategoryId)),
					Expense.class);

			assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(updateResponse.getBody()).isNotNull();
			assertThat(updateResponse.getBody().getId()).isEqualTo(expense.getId());
			assertThat(updateResponse.getBody().getCategoryId()).isEqualTo(targetCategoryId);

			ResponseEntity<Expense> getByIdResponse = restTemplate.getForEntity(
					"http://localhost:" + port + "/expenses/" + expense.getId(),
					Expense.class);

			assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(getByIdResponse.getBody()).isNotNull();
			assertThat(getByIdResponse.getBody().getCategoryId()).isEqualTo(targetCategoryId);
		} finally {
			if (originalCategoryId != null && originalCategoryId > 0 && !originalCategoryId.equals(targetCategoryId)) {
				restTemplate.exchange(
						url,
						HttpMethod.PUT,
						new HttpEntity<>(Map.of("categoryId", originalCategoryId)),
						Expense.class);
			}
		}
	}

	@Test
	void createExpensePersistsAndReturnsExpense() {
		// Ensure at least one category exists
		List<Category> categories = categoryRepository.findAll();
		Category category;
		if (categories.isEmpty()) {
			category = new Category();
			category.setName("Test Category");
			category = categoryRepository.save(category);
		} else {
			category = categories.get(0);
		}

		String url = "http://localhost:" + port + "/expenses";

		int amount = 123;
		String description = "Integration test expense";

		ResponseEntity<Expense> response = restTemplate.postForEntity(
			url,
			Map.of(
				"amount", amount,
				"description", description,
				"categoryId", category.getId()
			),
			Expense.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Expense created = response.getBody();
		assertThat(created).isNotNull();
		assertThat(created.getId()).isNotNull();
		assertThat(created.getAmount()).isEqualTo(amount);
		assertThat(created.getDescription()).isEqualTo(description);
		assertThat(created.getCategoryId()).isEqualTo(category.getId());

		// Fetch by ID to verify persistence
		ResponseEntity<Expense> getById = restTemplate.getForEntity(
			"http://localhost:" + port + "/expenses/" + created.getId(),
			Expense.class
		);
		assertThat(getById.getStatusCode()).isEqualTo(HttpStatus.OK);
		Expense fetched = getById.getBody();
		assertThat(fetched).isNotNull();
		assertThat(fetched.getId()).isEqualTo(created.getId());
		assertThat(fetched.getAmount()).isEqualTo(amount);
		assertThat(fetched.getDescription()).isEqualTo(description);
		assertThat(fetched.getCategoryId()).isEqualTo(category.getId());
	}
}
