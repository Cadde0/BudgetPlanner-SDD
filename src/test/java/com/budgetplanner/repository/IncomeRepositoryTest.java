package com.budgetplanner.repository;

import com.budgetplanner.model.Income;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = com.budgetplanner.BudgetPlannerApplication.class)
class IncomeRepositoryTest {

    private static final int KNOWN_INCOME_ID = 20;
    private static final int EXPECTED_INCOME_AMOUNT = 3000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private IncomeRepository incomeRepository;

    @BeforeEach
    void setUp() {
        incomeRepository = new IncomeRepository(jdbcTemplate);
    }

    @Test
    void findAllContainsConfiguredIncomeFixture() {
        assumeTrue(KNOWN_INCOME_ID > 0, "Set KNOWN_INCOME_ID to an existing row in your database");

        List<Income> result = incomeRepository.findAll();

        assertTrue(result.stream().anyMatch(income -> KNOWN_INCOME_ID == income.getId()));
    }

    @Test
    void findByIdReturnsConfiguredIncomeFixture() {
        assumeTrue(KNOWN_INCOME_ID > 0, "Set KNOWN_INCOME_ID to an existing row in your database");
        assumeTrue(EXPECTED_INCOME_AMOUNT >= 0, "Set EXPECTED_INCOME_AMOUNT for that fixture row");

        var result = incomeRepository.findById(KNOWN_INCOME_ID);

        assertTrue(result.isPresent());
        assertEquals(EXPECTED_INCOME_AMOUNT, result.get().getAmount());
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        var result = incomeRepository.findById(Integer.MAX_VALUE);

        assertFalse(result.isPresent());
    }

    @Test
    void savePersistsIncomeAndReturnsGeneratedId() {
        Income created = incomeRepository.save(new Income(null, 3456));

        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertTrue(incomeRepository.findById(created.getId()).isPresent());
        assertEquals(3456, incomeRepository.findById(created.getId()).get().getAmount());
    }

    @Test
    void updateChangesExistingIncome() {
        Income created = incomeRepository.save(new Income(null, 1234));

        var updated = incomeRepository.update(created.getId(), new Income(null, 5678));

        assertTrue(updated.isPresent());
        assertEquals(created.getId(), updated.get().getId());
        assertEquals(5678, updated.get().getAmount());
        assertEquals(5678, incomeRepository.findById(created.getId()).get().getAmount());
    }

    @Test
    void deleteByIdRemovesIncome() {
        Income created = incomeRepository.save(new Income(null, 2222));

        boolean deleted = incomeRepository.deleteById(created.getId());

        assertTrue(deleted);
        assertFalse(incomeRepository.findById(created.getId()).isPresent());
    }
}
