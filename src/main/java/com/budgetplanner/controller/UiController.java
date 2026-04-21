package com.budgetplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the single-page UI for the budget planner dashboard.
 */
@Controller
public class UiController {

    @GetMapping({ "/", "/budget" })
    public String home() {
        return "forward:/index.html";
    }
}