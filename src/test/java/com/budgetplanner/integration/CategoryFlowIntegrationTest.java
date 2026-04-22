package com.budgetplanner.integration;

import com.budgetplanner.model.Category;
import java.util.Arrays;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.budgetplanner.BudgetPlannerApplication.class)
class CategoryFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void categoryCrudFlowPersistsAndReturnsExpectedValues() {
        String baseUrl = "http://localhost:" + port + "/categories";

        Integer createdId = null;
        try {
            Category createPayload = new Category(null, "T022 Category", 2500, "created by integration test");
            ResponseEntity<Category> createResponse = restTemplate.postForEntity(baseUrl, createPayload, Category.class);

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createResponse.getBody()).isNotNull();
            assertThat(createResponse.getBody().getId()).isNotNull();
            assertThat(createResponse.getBody().getName()).isEqualTo("T022 Category");

            createdId = createResponse.getBody().getId();

            ResponseEntity<Category[]> getAllAfterCreate = restTemplate.getForEntity(baseUrl, Category[].class);
            assertThat(getAllAfterCreate.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getAllAfterCreate.getBody()).isNotNull();
            final Integer createdCategoryId = createdId;
            assertThat(Arrays.asList(getAllAfterCreate.getBody()))
                    .anyMatch(category -> createdCategoryId.equals(category.getId())
                            && "T022 Category".equals(category.getName()));

            Category updatePayload = new Category(null, "T022 Category Updated", 3200, "updated by integration test");
            ResponseEntity<Category> updateResponse = restTemplate.exchange(
                    baseUrl + "/" + createdId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updatePayload),
                    Category.class);

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody()).isNotNull();
            assertThat(updateResponse.getBody().getId()).isEqualTo(createdId);
            assertThat(updateResponse.getBody().getName()).isEqualTo("T022 Category Updated");
            assertThat(updateResponse.getBody().getCategoryLimit()).isEqualTo(3200);

            ResponseEntity<Category[]> getAllAfterUpdate = restTemplate.getForEntity(baseUrl, Category[].class);
            assertThat(getAllAfterUpdate.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getAllAfterUpdate.getBody()).isNotNull();
            assertThat(Arrays.asList(getAllAfterUpdate.getBody()))
                    .anyMatch(category -> createdCategoryId.equals(category.getId())
                            && "T022 Category Updated".equals(category.getName())
                            && Integer.valueOf(3200).equals(category.getCategoryLimit()));

            ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                    baseUrl + "/" + createdId,
                    HttpMethod.DELETE,
                    null,
                    Void.class);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            ResponseEntity<Category[]> getAllAfterDelete = restTemplate.getForEntity(baseUrl, Category[].class);
            assertThat(getAllAfterDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getAllAfterDelete.getBody()).isNotNull();
            assertThat(Arrays.asList(getAllAfterDelete.getBody()))
                    .noneMatch(category -> createdCategoryId.equals(category.getId()));

            createdId = null;
        } finally {
            if (createdId != null) {
                restTemplate.exchange(baseUrl + "/" + createdId, HttpMethod.DELETE, null, Void.class);
            }
        }
    }
}
