package com.budgetplanner.controller;

import com.budgetplanner.model.Income;
import com.budgetplanner.repository.IncomeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/income")
public class IncomeController {
    private final IncomeRepository incomeRepository;

    public IncomeController(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @GetMapping
    public List<Income> getAllIncome() {
        return incomeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Income> getIncomeById(@PathVariable int id) {
        return incomeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
