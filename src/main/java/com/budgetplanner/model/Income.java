package com.budgetplanner.model;

/**
 * Represents an income entry used for budget calculations.
 */
public class Income {
    private Integer id;
    private Integer amount;

    /**
     * Creates an empty income entry.
     */
    public Income() {
    }

    /**
     * Creates an income entry with the supplied values.
     *
     * @param id the income identifier
     * @param amount the income amount
     */
    public Income(Integer id, Integer amount) {
        this.id = id;
        this.amount = amount;
    }

    /**
     * Returns the income identifier.
     *
     * @return the income identifier
     */
    public Integer getId() {
        return id;
    }

    /**
     * Updates the income identifier.
     *
     * @param id the income identifier
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Returns the income amount.
     *
     * @return the income amount
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Updates the income amount.
     *
     * @param amount the income amount
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
