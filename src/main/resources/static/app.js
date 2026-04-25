const state = {
  incomes: [],
  categories: [],
  summaries: [],
  expenses: [],
  budgetSnapshot: null,
  budgetSyncedAt: null,
  budgetPollTimer: null,
  editingIncomeId: null,
  editingCategoryId: null,
  editingExpenseId: null,
};

const elements = {
  viewSwitcher: document.querySelector(".view-switcher"),
  viewButtons: Array.from(document.querySelectorAll(".view-button")),
  statusBanner: document.getElementById("statusBanner"),
  heroStats: document.getElementById("heroStats"),
  budgetSyncStatus: document.getElementById("budgetSyncStatus"),
  incomeTableBody: document.getElementById("incomeTableBody"),
  incomeForm: document.getElementById("incomeForm"),
  incomeId: document.getElementById("incomeId"),
  incomeAmount: document.getElementById("incomeAmount"),
  incomeSubmitButton: document.getElementById("incomeSubmitButton"),
  incomeCancelButton: document.getElementById("incomeCancelButton"),
  refreshIncomeButton: document.getElementById("refreshIncomeButton"),
  refreshCategoryButton: document.getElementById("refreshCategoryButton"),
  categoryForm: document.getElementById("categoryForm"),
  categoryId: document.getElementById("categoryId"),
  categoryName: document.getElementById("categoryName"),
  categoryLimit: document.getElementById("categoryLimit"),
  categoryDescription: document.getElementById("categoryDescription"),
  categorySubmitButton: document.getElementById("categorySubmitButton"),
  categoryCancelButton: document.getElementById("categoryCancelButton"),
  categoryTableBody: document.getElementById("categoryTableBody"),
  categoryExpensePanels: document.getElementById("categoryExpensePanels"),
  refreshExpenseButton: document.getElementById("refreshExpenseButton"),
  // Expense add form
  expenseForm: document.getElementById("expenseForm"),
  expenseId: document.getElementById("expenseId"),
  expenseAmount: document.getElementById("expenseAmount"),
  expenseDescription: document.getElementById("expenseDescription"),
  expenseCategorySelect: document.getElementById("expenseCategorySelect"),
  newCategoryLabel: document.getElementById("newCategoryLabel"),
  newCategoryName: document.getElementById("newCategoryName"),
  expenseSubmitButton: document.getElementById("expenseSubmitButton"),
  expenseCancelButton: document.getElementById("expenseCancelButton"),
};

function setPanelView(view) {
  document.body.dataset.panelView = view;
  elements.viewButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.view === view);
  });
}

function initViewSwitcher() {
  if (!elements.viewButtons.length) {
    return;
  }

  setPanelView("all");
  elements.viewButtons.forEach((button) => {
    button.addEventListener("click", () => {
      setPanelView(button.dataset.view || "all");
    });
  });
}
function populateExpenseCategorySelect() {
  const select = elements.expenseCategorySelect;
  if (!select) return;
  // Remove all except the first and last ("Select category" and "+ New category...")
  while (select.options.length > 2) {
    select.remove(1);
  }
  state.categories.forEach((cat) => {
    const opt = document.createElement("option");
    opt.value = cat.id;
    opt.textContent = cat.name;
    select.insertBefore(opt, select.options[select.options.length - 1]);
  });
}

function handleExpenseCategoryChange() {
  const select = elements.expenseCategorySelect;
  const showNew = select.value === "__new__";
  elements.newCategoryLabel.style.display = showNew ? "block" : "none";
  if (!showNew) elements.newCategoryName.value = "";
}

async function handleExpenseFormSubmit(event) {
  event.preventDefault();
  const amount = Number.parseInt(elements.expenseAmount.value, 10);
  const description = elements.expenseDescription.value.trim();
  const categoryValue = elements.expenseCategorySelect.value;
  let categoryId = null;

  if (!Number.isInteger(amount) || amount <= 0) {
    setStatus("Expense amount must be a positive whole number.", "error");
    return;
  }

  if (!categoryValue) {
    setStatus("Please select a category or create a new one.", "error");
    return;
  }

  if (categoryValue === "__new__") {
    const newName = elements.newCategoryName.value.trim();
    if (!newName) {
      setStatus("Please enter a name for the new category.", "error");
      return;
    }
    // Create category first
    try {
      const newCat = await requestJson("/categories", {
        method: "POST",
        body: JSON.stringify({ name: newName }),
      });
      categoryId = newCat.id;
      await refreshDashboard("Category created and selected.");
    } catch (error) {
      setStatus(error.message, "error");
      return;
    }
  } else {
    categoryId = Number(categoryValue);
  }

  // Now create the expense
  try {
    if (state.editingExpenseId == null) {
      await requestJson("/expenses", {
        method: "POST",
        body: JSON.stringify({ amount, description, categoryId }),
      });
      setStatus("Expense added successfully.", "success");
    } else {
      await requestJson(`/expenses/${state.editingExpenseId}`, {
        method: "PUT",
        body: JSON.stringify({ amount, description, categoryId }),
      });
      setStatus("Expense updated successfully.", "success");
    }

    resetExpenseForm();
    await refreshDashboard();
  } catch (error) {
    setStatus(error.message, "error");
  }
}

function resetExpenseForm() {
  state.editingExpenseId = null;
  elements.expenseId.value = "";
  elements.expenseAmount.value = "";
  elements.expenseDescription.value = "";
  elements.expenseCategorySelect.value = "";
  elements.newCategoryName.value = "";
  elements.newCategoryLabel.style.display = "none";
  elements.expenseSubmitButton.textContent = "Add expense";
  elements.expenseCancelButton.hidden = true;
}

function beginExpenseEdit(expenseId) {
  const expense = state.expenses.find((entry) => entry.id === expenseId);
  if (!expense) {
    setStatus("The expense entry no longer exists.", "error");
    return;
  }

  state.editingExpenseId = expenseId;
  elements.expenseId.value = String(expenseId);
  elements.expenseAmount.value = String(expense.amount ?? "");
  elements.expenseDescription.value = expense.description || "";
  elements.expenseCategorySelect.value =
    expense.categoryId == null ? "" : String(expense.categoryId);
  elements.newCategoryName.value = "";
  elements.newCategoryLabel.style.display = "none";
  elements.expenseSubmitButton.textContent = "Update expense";
  elements.expenseCancelButton.hidden = false;
  elements.expenseAmount.focus();
}

function renderExpenseTable() {
  if (!elements.categoryExpensePanels) {
    return;
  }

  if (!state.categories.length) {
    elements.categoryExpensePanels.innerHTML =
      '<div class="loading-state">No categories found.</div>';
    return;
  }

  const totalCategorySpend = state.summaries.reduce(
    (sum, item) => sum + Number(item.totalExpense || 0),
    0,
  );

  const summaryByCategoryId = new Map(
    state.summaries.map((summary) => [Number(summary.categoryId), summary]),
  );

  elements.categoryExpensePanels.innerHTML = state.categories
    .map((category) => {
      const categoryId = Number(category.id);
      const categoryExpenses = state.expenses.filter(
        (expense) => Number(expense.categoryId) === categoryId,
      );
      const fallbackSpend = categoryExpenses.reduce(
        (sum, expense) => sum + Number(expense.amount || 0),
        0,
      );
      const summary = summaryByCategoryId.get(categoryId);
      const categorySpend = Number(summary?.totalExpense ?? fallbackSpend);
      const spendShare =
        totalCategorySpend > 0
          ? Math.round((categorySpend / totalCategorySpend) * 100)
          : 0;
      const categoryLimit =
        category.categoryLimit == null ? null : Number(category.categoryLimit);
      const limitUsage =
        categoryLimit != null && categoryLimit > 0
          ? Math.round((categorySpend / categoryLimit) * 100)
          : null;
      const isOverLimit = limitUsage != null && limitUsage > 100;
      const remainingLimit =
        categoryLimit == null
          ? null
          : Math.max(categoryLimit - categorySpend, 0);
      const overLimitClass = isOverLimit ? ' class="is-over-limit"' : "";
      const limitUsageMarkup =
        limitUsage == null
          ? ""
          : `<span${overLimitClass}><strong${overLimitClass}>${limitUsage}%</strong> of limit</span>`;

      const expenseRows =
        categoryExpenses.length === 0
          ? '<tr><td colspan="3" class="empty-state">No expenses in this category.</td></tr>'
          : categoryExpenses
              .map((expense) => {
                const expenseCategoryId = Number(expense.categoryId);
                const rowCategoryOptions = state.categories
                  .map((cat) => {
                    const selected =
                      Number(cat.id) === expenseCategoryId ? " selected" : "";
                    return `<option value="${cat.id}"${selected}>${escapeHtml(cat.name)}</option>`;
                  })
                  .join("");

                return `
                  <tr>
                      <td data-label="Description">${escapeHtml(expense.description || "No description")}</td>
                      <td data-label="Amount">${formatAmount(expense.amount)}</td>
                      <td data-label="Actions">
                        <div class="row-actions">
                          <select class="row-category-select" data-role="category-select" data-id="${expense.id}" aria-label="Select category for expense ${expense.id}">
                            <option value="">Select category</option>
                            ${rowCategoryOptions}
                          </select>
                          <button class="row-action" type="button" data-action="assign-category" data-id="${expense.id}">Assign</button>
                          <button class="row-action" type="button" data-action="edit-expense" data-id="${expense.id}">Edit</button>
                          <button class="row-action" type="button" data-action="delete-expense" data-id="${expense.id}" data-tone="danger">Delete</button>
                        </div>
                      </td>
                  </tr>
                `;
              })
              .join("");

      return `
        <article class="category-expense-panel" aria-label="${escapeHtml(category.name)} expenses">
          <div class="panel-header category-expense-panel-header">
            <div>
              <p class="panel-kicker">${escapeHtml(category.name)}</p>
            </div>
            <span class="category-panel-total">${formatAmount(categorySpend)}</span>
          </div>

          <div class="category-summary-strip">
            <span><strong>${spendShare}%</strong> of spend</span>
            <span><strong>${categoryLimit == null ? "Not set" : formatAmount(categoryLimit)}</strong> limit</span>
            <span><strong>${remainingLimit == null ? "N/A" : formatAmount(remainingLimit)}</strong> left</span>
            ${limitUsageMarkup}
          </div>

          ${limitUsage == null ? "" : `<div class="category-meter" aria-hidden="true"><span style="width: ${Math.min(limitUsage, 100)}%"></span></div>`}

          <div class="table-wrap">
            <table class="data-table expense-table" aria-label="${escapeHtml(category.name)} expense entries">
              <thead>
                <tr>
                  <th>Description</th>
                  <th>Amount</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                ${expenseRows}
              </tbody>
            </table>
          </div>
        </article>
      `;
    })
    .join("");
}

async function loadExpenses() {
  state.expenses = await requestJson("/expenses");
  renderExpenseTable();
}

async function handleExpenseTableClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }

  const expenseId = Number(button.dataset.id);
  const action = button.dataset.action;

  try {
    if (action === "assign-category") {
      const row = button.closest("tr");
      const select = row?.querySelector('select[data-role="category-select"]');
      const categoryId = Number.parseInt(select?.value || "", 10);

      if (!Number.isInteger(categoryId) || categoryId <= 0) {
        setStatus("Please select a valid category before assigning.", "error");
        return;
      }

      await requestJson(`/expenses/${expenseId}/category`, {
        method: "PUT",
        body: JSON.stringify({ categoryId }),
      });
      await refreshDashboard("Category assigned successfully.");
      return;
    }

    if (action === "edit-expense") {
      beginExpenseEdit(expenseId);
      return;
    }

    if (action === "delete-expense") {
      const confirmed = globalThis.confirm(`Delete expense #${expenseId}?`);
      if (!confirmed) {
        return;
      }

      await requestJson(`/expenses/${expenseId}`, { method: "DELETE" });
      if (state.editingExpenseId === expenseId) {
        resetExpenseForm();
      }
      await refreshDashboard("Expense deleted successfully.");
      return;
    }

    if (
      action !== "assign-category" &&
      action !== "edit-expense" &&
      action !== "delete-expense"
    ) {
      return;
    }
  } catch (error) {
    setStatus(error.message, "error");
  }
}

const currencyFormatter = new Intl.NumberFormat("sv-SE");

async function requestJson(url, options = {}) {
  const headers = {
    Accept: "application/json",
    "Content-Type": "application/json",
  };

  if (options.headers) {
    Object.assign(headers, options.headers);
  }

  const response = await fetch(url, {
    headers,
    ...options,
  });

  if (!response.ok) {
    const details = await response.text();
    throw new Error(
      details || `Request to ${url} failed with status ${response.status}`,
    );
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    return response.json();
  }

  return response.text();
}

function setStatus(message, tone = "") {
  elements.statusBanner.className = `banner ${tone}`.trim();
  elements.statusBanner.textContent = message;
}

function formatAmount(value) {
  return `${currencyFormatter.format(Number(value || 0))} kr`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function renderHeroStats() {
  const budget = state.budgetSnapshot || getLocalBudgetSnapshot();
  const totalIncome = Number(budget.totalIncome || 0);
  const totalExpense = Number(budget.totalExpense || 0);
  const remaining = Number(
    budget.remainingBudget ?? totalIncome - totalExpense,
  );

  elements.heroStats.innerHTML = `
        <article class="stat-card">
            <span class="stat-label">Stored income</span>
            <div class="stat-value">${formatAmount(totalIncome)}</div>
        </article>
        <article class="stat-card">
            <span class="stat-label">Total expense</span>
            <div class="stat-value">${formatAmount(totalExpense)}</div>
        </article>
        <article class="stat-card">
            <span class="stat-label">Budget remainder</span>
            <div class="stat-value">${formatAmount(remaining)}</div>
        </article>
    `;

  if (elements.budgetSyncStatus) {
    elements.budgetSyncStatus.textContent = state.budgetSyncedAt
      ? `Live budget updated at ${new Date(state.budgetSyncedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}.`
      : "Live budget updates automatically as income and expenses change.";
  }
}

function getLocalBudgetSnapshot() {
  const totalIncome = state.incomes.reduce(
    (sum, income) => sum + Number(income.amount || 0),
    0,
  );
  const totalExpense = state.expenses.reduce(
    (sum, expense) => sum + Number(expense.amount || 0),
    0,
  );

  return {
    totalIncome,
    totalExpense,
    remainingBudget: totalIncome - totalExpense,
  };
}

async function loadBudgetSnapshot(options = {}) {
  try {
    const snapshot = await requestJson("/budget/remaining");
    state.budgetSnapshot = snapshot;
    state.budgetSyncedAt = Date.now();
    renderHeroStats();
    return snapshot;
  } catch (error) {
    if (!state.budgetSnapshot) {
      renderHeroStats();
    }

    if (!options.silent) {
      throw error;
    }

    console.warn("Budget snapshot refresh failed:", error);
    return null;
  }
}

function startBudgetPolling() {
  if (state.budgetPollTimer != null) {
    clearInterval(state.budgetPollTimer);
  }

  state.budgetPollTimer = globalThis.setInterval(() => {
    loadBudgetSnapshot({ silent: true });
  }, 5000);
}

function renderIncomeTable() {
  if (!state.incomes.length) {
    elements.incomeTableBody.innerHTML =
      '<tr><td colspan="3" class="empty-state">No income has been saved yet.</td></tr>';
    return;
  }

  elements.incomeTableBody.innerHTML = state.incomes
    .map(
      (income) => `
        <tr>
            <td data-label="Amount">${formatAmount(income.amount)}</td>
            <td data-label="Actions">
                <div class="row-actions">
                    <button class="row-action" type="button" data-action="edit-income" data-id="${income.id}">Edit</button>
                    <button class="row-action" type="button" data-action="delete-income" data-id="${income.id}" data-tone="danger">Delete</button>
                </div>
            </td>
        </tr>
    `,
    )
    .join("");
}

function renderCategories() {
  if (!state.categories.length) {
    elements.categoryTableBody.innerHTML =
      '<tr><td colspan="4" class="empty-state">No categories are available yet.</td></tr>';
    return;
  }

  elements.categoryTableBody.innerHTML = state.categories
    .map((category) => {
      const limit =
        category.categoryLimit == null ? null : Number(category.categoryLimit);

      return `
        <tr>
          <td data-label="Name">${category.name}</td>
          <td data-label="Limit">${limit == null ? "Not set" : formatAmount(limit)}</td>
          <td data-label="Description">${category.description || "No description"}</td>
          <td data-label="Actions">
                <div class="row-actions">
                    <button class="row-action" type="button" data-action="edit-category" data-id="${category.id}">Edit</button>
                    <button class="row-action" type="button" data-action="delete-category" data-id="${category.id}" data-tone="danger">Delete</button>
                </div>
            </td>
        </tr>
        `;
    })
    .join("");
}

function resetCategoryForm() {
  state.editingCategoryId = null;
  elements.categoryId.value = "";
  elements.categoryName.value = "";
  elements.categoryLimit.value = "";
  elements.categoryDescription.value = "";
  elements.categorySubmitButton.textContent = "Save category";
  elements.categoryCancelButton.hidden = true;
}

function beginCategoryEdit(categoryId) {
  const category = state.categories.find((entry) => entry.id === categoryId);
  if (!category) {
    setStatus("The category no longer exists.", "error");
    return;
  }

  state.editingCategoryId = categoryId;
  elements.categoryId.value = String(categoryId);
  elements.categoryName.value = category.name || "";
  elements.categoryLimit.value =
    category.categoryLimit == null ? "" : String(category.categoryLimit);
  elements.categoryDescription.value = category.description || "";
  elements.categorySubmitButton.textContent = "Update category";
  elements.categoryCancelButton.hidden = false;
  elements.categoryName.focus();
}

async function handleCategorySubmit(event) {
  event.preventDefault();

  const name = elements.categoryName.value.trim();
  const description = elements.categoryDescription.value.trim();
  const limitRaw = elements.categoryLimit.value.trim();
  const categoryLimit =
    limitRaw.length === 0 ? null : Number.parseInt(limitRaw, 10);

  if (!name) {
    setStatus("Category name is required.", "error");
    return;
  }

  if (
    categoryLimit !== null &&
    (!Number.isInteger(categoryLimit) || categoryLimit < 0)
  ) {
    setStatus("Category limit must be a non-negative whole number.", "error");
    return;
  }

  const payload = {
    name,
    categoryLimit,
    description,
  };

  try {
    if (state.editingCategoryId == null) {
      await requestJson("/categories", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setStatus("Category added successfully.", "success");
    } else {
      await requestJson(`/categories/${state.editingCategoryId}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      setStatus("Category updated successfully.", "success");
    }

    resetCategoryForm();
    await refreshDashboard();
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function handleCategoryTableClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }

  const categoryId = Number(button.dataset.id);
  const action = button.dataset.action;

  try {
    if (action === "edit-category") {
      beginCategoryEdit(categoryId);
      return;
    }

    if (action === "delete-category") {
      const confirmed = globalThis.confirm(`Delete category #${categoryId}?`);
      if (!confirmed) {
        return;
      }

      await requestJson(`/categories/${categoryId}`, { method: "DELETE" });
      if (state.editingCategoryId === categoryId) {
        resetCategoryForm();
      }
      await refreshDashboard("Category deleted successfully.");
    }
  } catch (error) {
    setStatus(error.message, "error");
  }
}

function resetIncomeForm() {
  state.editingIncomeId = null;
  elements.incomeId.value = "";
  elements.incomeAmount.value = "";
  elements.incomeSubmitButton.textContent = "Save income";
  elements.incomeCancelButton.hidden = true;
}

function beginIncomeEdit(incomeId) {
  const income = state.incomes.find((entry) => entry.id === incomeId);
  if (!income) {
    setStatus("The income entry no longer exists.", "error");
    return;
  }

  state.editingIncomeId = incomeId;
  elements.incomeId.value = String(incomeId);
  elements.incomeAmount.value = String(income.amount ?? "");
  elements.incomeSubmitButton.textContent = "Update income";
  elements.incomeCancelButton.hidden = false;
  elements.incomeAmount.focus();
}

async function loadIncome() {
  state.incomes = await requestJson("/income");
  renderIncomeTable();
  renderHeroStats();
}

async function loadCategories() {
  const [categories, summaries] = await Promise.all([
    requestJson("/categories"),
    requestJson("/categories/summaries"),
  ]);

  state.categories = categories;
  state.summaries = summaries;
  renderCategories();
  renderExpenseTable();
  renderHeroStats();
  populateExpenseCategorySelect();
}

async function refreshDashboard(message = "Data refreshed.") {
  await Promise.all([loadIncome(), loadCategories(), loadExpenses()]);
  await loadBudgetSnapshot();
  setStatus(message, "success");
}

async function handleIncomeSubmit(event) {
  event.preventDefault();

  const amount = Number.parseInt(elements.incomeAmount.value, 10);
  if (!Number.isInteger(amount) || amount <= 0) {
    setStatus("Income must be a positive whole number.", "error");
    return;
  }

  const payload = { amount };

  try {
    if (state.editingIncomeId == null) {
      await requestJson("/income", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setStatus("Income added successfully.", "success");
    } else {
      await requestJson(`/income/${state.editingIncomeId}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      setStatus("Income updated successfully.", "success");
    }

    resetIncomeForm();
    await refreshDashboard();
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function handleIncomeTableClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }

  const incomeId = Number(button.dataset.id);
  const action = button.dataset.action;

  try {
    if (action === "edit-income") {
      beginIncomeEdit(incomeId);
      return;
    }

    if (action === "delete-income") {
      const confirmed = globalThis.confirm(`Delete income #${incomeId}?`);
      if (!confirmed) {
        return;
      }

      await requestJson(`/income/${incomeId}`, { method: "DELETE" });
      if (state.editingIncomeId === incomeId) {
        resetIncomeForm();
      }
      await refreshDashboard("Income removed successfully.");
    }
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function start() {
  initViewSwitcher();
  elements.incomeForm.addEventListener("submit", handleIncomeSubmit);
  elements.incomeTableBody.addEventListener("click", handleIncomeTableClick);
  elements.refreshIncomeButton.addEventListener("click", () =>
    refreshDashboard("Income refreshed."),
  );
  elements.refreshCategoryButton.addEventListener("click", () =>
    refreshDashboard("Category data refreshed."),
  );
  elements.categoryForm.addEventListener("submit", handleCategorySubmit);
  elements.categoryTableBody.addEventListener(
    "click",
    handleCategoryTableClick,
  );
  elements.categoryCancelButton.addEventListener("click", () => {
    resetCategoryForm();
    setStatus("Category edit cancelled.", "success");
  });
  elements.incomeCancelButton.addEventListener("click", () => {
    resetIncomeForm();
    setStatus("Income edit cancelled.", "success");
  });

  if (elements.categoryExpensePanels) {
    elements.categoryExpensePanels.addEventListener(
      "click",
      handleExpenseTableClick,
    );
  }
  elements.refreshExpenseButton.addEventListener("click", () =>
    refreshDashboard("Expense data refreshed."),
  );

  // Expense add form events
  if (elements.expenseForm) {
    elements.expenseForm.addEventListener("submit", handleExpenseFormSubmit);
    elements.expenseCategorySelect.addEventListener(
      "change",
      handleExpenseCategoryChange,
    );
    elements.expenseCancelButton.addEventListener("click", () => {
      resetExpenseForm();
      setStatus("Expense edit cancelled.", "success");
    });
  }

  try {
    await refreshDashboard("Dashboard loaded.");
    startBudgetPolling();
  } catch (error) {
    setStatus(error.message, "error");
  }
}

document.addEventListener("DOMContentLoaded", start);
