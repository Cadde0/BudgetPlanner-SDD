package com.budgetplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the budget planner service.
 */
@SpringBootApplication
public class BudgetPlannerApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(BudgetPlannerApplication.class, args);
    }
}
