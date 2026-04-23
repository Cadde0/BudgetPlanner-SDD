package com.budgetplanner.integration;

import com.budgetplanner.model.Category;
import com.budgetplanner.model.Expense;
import com.budgetplanner.repository.CategoryRepository;
import com.budgetplanner.repository.ExpenseRepository;
import java.util.HashMap;
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

	@Test
	void updateExpenseChangesAmountDescriptionAndCategory() {
		List<Expense> expenses = expenseRepository.findAll();
		List<Category> categories = categoryRepository.findAll();

		assumeTrue(!expenses.isEmpty(), "Test requires at least one existing expense row");
		assumeTrue(!categories.isEmpty(), "Test requires at least one existing category row");

		Expense original = expenses.get(0);

		Integer targetCategoryId = categories.stream()
				.map(Category::getId)
				.filter(id -> id != null && id > 0)
				.filter(id -> !id.equals(original.getCategoryId()))
				.findFirst()
				.orElse(original.getCategoryId());

		int updatedAmount = original.getAmount() + 1;
		String updatedDescription = (original.getDescription() == null ? "" : original.getDescription()) + " updated";

		String url = "http://localhost:" + port + "/expenses/" + original.getId();

		try {
			Map<String, Object> updatePayload = new HashMap<>();
			updatePayload.put("amount", updatedAmount);
			updatePayload.put("description", updatedDescription);
			updatePayload.put("categoryId", targetCategoryId);

			ResponseEntity<Expense> updateResponse = restTemplate.exchange(
					url,
					HttpMethod.PUT,
					new HttpEntity<>(updatePayload),
					Expense.class);

			assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(updateResponse.getBody()).isNotNull();
			assertThat(updateResponse.getBody().getId()).isEqualTo(original.getId());
			assertThat(updateResponse.getBody().getAmount()).isEqualTo(updatedAmount);
			assertThat(updateResponse.getBody().getDescription()).isEqualTo(updatedDescription);
			assertThat(updateResponse.getBody().getCategoryId()).isEqualTo(targetCategoryId);

			ResponseEntity<Expense> getByIdResponse = restTemplate.getForEntity(
					url,
					Expense.class);

			assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(getByIdResponse.getBody()).isNotNull();
			assertThat(getByIdResponse.getBody().getAmount()).isEqualTo(updatedAmount);
			assertThat(getByIdResponse.getBody().getDescription()).isEqualTo(updatedDescription);
			assertThat(getByIdResponse.getBody().getCategoryId()).isEqualTo(targetCategoryId);
		} finally {
			Map<String, Object> restorePayload = new HashMap<>();
			restorePayload.put("amount", original.getAmount());
			restorePayload.put("description", original.getDescription());
			restorePayload.put("categoryId", original.getCategoryId());

			restTemplate.exchange(
					url,
					HttpMethod.PUT,
					new HttpEntity<>(restorePayload),
					Expense.class);
		}
	}

	@Test
	void deleteExpenseRemovesExpenseAndReturnsNotFoundAfterwards() {
		String baseUrl = "http://localhost:" + port + "/expenses";

		Integer createdExpenseId = null;
		Integer temporaryCategoryId = null;

		try {
			List<Category> categories = categoryRepository.findAll();
			Integer categoryId;
			if (categories.isEmpty()) {
				Category createdCategory = categoryRepository
						.save(new Category(null, "T030 Category", null, "created for expense delete integration test"));
				temporaryCategoryId = createdCategory.getId();
				categoryId = createdCategory.getId();
			} else {
				categoryId = categories.get(0).getId();
			}

			Map<String, Object> createPayload = new HashMap<>();
			createPayload.put("amount", 321);
			createPayload.put("description", "T030 delete integration expense");
			createPayload.put("categoryId", categoryId);

			ResponseEntity<Expense> createResponse = restTemplate.postForEntity(baseUrl, createPayload, Expense.class);
			assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThat(createResponse.getBody()).isNotNull();
			assertThat(createResponse.getBody().getId()).isNotNull();

			createdExpenseId = createResponse.getBody().getId();

			ResponseEntity<Void> deleteResponse = restTemplate.exchange(
					baseUrl + "/" + createdExpenseId,
					HttpMethod.DELETE,
					null,
					Void.class);
			assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

			ResponseEntity<Expense> getAfterDeleteResponse = restTemplate.getForEntity(
					baseUrl + "/" + createdExpenseId,
					Expense.class);
			assertThat(getAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

			ResponseEntity<Expense[]> getAllResponse = restTemplate.getForEntity(baseUrl, Expense[].class);
			assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(getAllResponse.getBody()).isNotNull();
			final Integer deletedExpenseId = createdExpenseId;
			assertThat(getAllResponse.getBody()).noneMatch(expense -> deletedExpenseId.equals(expense.getId()));

			createdExpenseId = null;
		} finally {
			if (createdExpenseId != null) {
				restTemplate.exchange(baseUrl + "/" + createdExpenseId, HttpMethod.DELETE, null, Void.class);
			}
			if (temporaryCategoryId != null) {
				categoryRepository.deleteById(temporaryCategoryId);
			}
		}
	}
}
