package com.budgetplanner.model;

public class Category {
    private Integer id;
    private String name;
    private Integer categoryLimit;
    private String description;

    public Category() {
    }

    public Category(Integer id, String name, Integer categoryLimit, String description) {
        this.id = id;
        this.name = name;
        this.categoryLimit = categoryLimit;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCategoryLimit() {
        return categoryLimit;
    }

    public void setCategoryLimit(Integer categoryLimit) {
        this.categoryLimit = categoryLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
