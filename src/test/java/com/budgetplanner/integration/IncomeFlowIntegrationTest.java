package com.budgetplanner.integration;

import com.budgetplanner.model.Income;
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
class IncomeFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void incomeCrudFlowPersistsAndReturnsExpectedValues() {
        String baseUrl = "http://localhost:" + port + "/income";

        Integer createdId = null;
        try {
            Income createPayload = new Income(null, 3210);
            ResponseEntity<Income> createResponse = restTemplate.postForEntity(baseUrl, createPayload, Income.class);

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createResponse.getBody()).isNotNull();
            assertThat(createResponse.getBody().getId()).isNotNull();
            assertThat(createResponse.getBody().getAmount()).isEqualTo(3210);

            createdId = createResponse.getBody().getId();

            ResponseEntity<Income> getByIdResponse = restTemplate.getForEntity(baseUrl + "/" + createdId, Income.class);
            assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getByIdResponse.getBody()).isNotNull();
            assertThat(getByIdResponse.getBody().getId()).isEqualTo(createdId);
            assertThat(getByIdResponse.getBody().getAmount()).isEqualTo(3210);

            Income updatePayload = new Income(null, 4321);
            ResponseEntity<Income> updateResponse = restTemplate.exchange(
                    baseUrl + "/" + createdId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updatePayload),
                    Income.class);

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody()).isNotNull();
            assertThat(updateResponse.getBody().getId()).isEqualTo(createdId);
            assertThat(updateResponse.getBody().getAmount()).isEqualTo(4321);

            ResponseEntity<Income[]> getAllResponse = restTemplate.getForEntity(baseUrl, Income[].class);
            assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getAllResponse.getBody()).isNotNull();
            final Integer persistedId = createdId;
            assertThat(getAllResponse.getBody())
                    .anyMatch(income -> persistedId.equals(income.getId())
                            && Integer.valueOf(4321).equals(income.getAmount()));

            ResponseEntity<Void> deleteResponse = restTemplate.exchange(baseUrl + "/" + createdId, HttpMethod.DELETE,
                    null,
                    Void.class);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            ResponseEntity<Income> getAfterDeleteResponse = restTemplate.getForEntity(baseUrl + "/" + createdId,
                    Income.class);
            assertThat(getAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

            createdId = null;
        } finally {
            if (createdId != null) {
                restTemplate.exchange(baseUrl + "/" + createdId, HttpMethod.DELETE, null, Void.class);
            }
        }
    }
}