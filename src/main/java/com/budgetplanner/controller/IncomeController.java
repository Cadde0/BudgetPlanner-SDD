package com.budgetplanner.controller;

import com.budgetplanner.application.IncomeService;
import com.budgetplanner.model.Income;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Exposes income operations over HTTP.
 */
@RestController
@RequestMapping("/income")
public class IncomeController {
    private final IncomeService incomeService;

    /**
     * Creates an income controller backed by the supplied income service.
     *
     * @param incomeService the income service
     */
    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    /**
     * Returns all income entries.
     *
     * @return all income entries
     */
    @GetMapping
    public List<Income> getAllIncome() {
        return incomeService.getAllIncome();
    }

    /**
     * Returns the income entry with the supplied identifier.
     *
     * @param id the income identifier
     * @return the matching income response, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Income> getIncomeById(@PathVariable int id) {
        return incomeService.getIncomeById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new income entry and returns the persisted record.
     *
     * @param income the income entry to create
     * @return the created income response
     */
    @PostMapping
    public ResponseEntity<Income> createIncome(@RequestBody Income income) {
        Income createdIncome = incomeService.createIncome(income);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdIncome.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdIncome);
    }

    /**
     * Updates an existing income entry.
     *
     * @param id the income identifier
     * @param income the updated income data
     * @return the updated income response, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Income> updateIncome(@PathVariable int id, @RequestBody Income income) {
        return incomeService.updateIncome(id, income)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes the income entry with the supplied identifier.
     *
     * @param id the income identifier
     * @return no content when deleted, or 404 when not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable int id) {
        if (incomeService.deleteIncome(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
