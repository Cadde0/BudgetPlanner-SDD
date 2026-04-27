package com.budgetplanner.model;

/**
 * Represents an expense category and its optional spending limit.
 */
public class Category {
    private Integer id;
    private String name;
    private Integer categoryLimit;
    private String description;

    /**
     * Creates an empty category.
     */
    public Category() {
    }

    /**
     * Creates a category with the supplied values.
     *
     * @param id the category identifier
     * @param name the category name
     * @param categoryLimit the spending limit for the category
     * @param description the category description
     */
    public Category(Integer id, String name, Integer categoryLimit, String description) {
        this.id = id;
        this.name = name;
        this.categoryLimit = categoryLimit;
        this.description = description;
    }

    /**
     * Returns the category identifier.
     *
     * @return the category identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Updates the category identifier.
     *
     * @param id the category identifier
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the category name.
     *
     * @return the category name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the category name.
     *
     * @param name the category name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the category spending limit.
     *
     * @return the category spending limit
     */
    public Integer getCategoryLimit() {
        return categoryLimit;
    }

    /**
     * Updates the category spending limit.
     *
     * @param categoryLimit the category spending limit
     */
    public void setCategoryLimit(Integer categoryLimit) {
        this.categoryLimit = categoryLimit;
    }

    /**
     * Returns the category description.
     *
     * @return the category description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the category description.
     *
     * @param description the category description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
