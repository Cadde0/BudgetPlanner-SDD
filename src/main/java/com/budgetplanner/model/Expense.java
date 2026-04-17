package com.budgetplanner.model;

public class Expense {
    private Integer id;
    private Integer amount;
    private Integer categoryId;
    private String description;

    public Expense() {
    }

    public Expense(Integer id, Integer amount, Integer categoryId, String description) {
        this.id = id;
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
