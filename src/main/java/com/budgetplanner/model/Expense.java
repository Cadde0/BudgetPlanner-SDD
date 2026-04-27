package com.budgetplanner.model;

/**
 * Represents a tracked expense and its category assignment.
 */
public class Expense {
    private Integer id;
    private Integer amount;
    private Integer categoryId;
    private String description;

    /**
     * Creates an empty expense.
     */
    public Expense() {
    }

    /**
     * Creates an expense with the supplied values.
     *
     * @param id the expense identifier
     * @param amount the expense amount
     * @param categoryId the category identifier for the expense
     * @param description the expense description
     */
    public Expense(Integer id, Integer amount, Integer categoryId, String description) {
        this.id = id;
        this.amount = amount;
        this.categoryId = categoryId;
        this.description = description;
    }

    /**
     * Returns the expense identifier.
     *
     * @return the expense identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Updates the expense identifier.
     *
     * @param id the expense identifier
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the expense amount.
     *
     * @return the expense amount
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Updates the expense amount.
     *
     * @param amount the expense amount
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * Returns the expense category identifier.
     *
     * @return the category identifier
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     * Updates the expense category identifier.
     *
     * @param categoryId the category identifier
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Returns the expense description.
     *
     * @return the expense description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the expense description.
     *
     * @param description the expense description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
