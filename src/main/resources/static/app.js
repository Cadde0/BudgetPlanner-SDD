const state = {
  incomes: [],
  categories: [],
  summaries: [],
  expenses: [],
  editingIncomeId: null,
  editingExpenseId: null,
};

const elements = {
  statusBanner: document.getElementById("statusBanner"),
  heroStats: document.getElementById("heroStats"),
  incomeTableBody: document.getElementById("incomeTableBody"),
  incomeForm: document.getElementById("incomeForm"),
  incomeId: document.getElementById("incomeId"),
  incomeAmount: document.getElementById("incomeAmount"),
  incomeSubmitButton: document.getElementById("incomeSubmitButton"),
  incomeCancelButton: document.getElementById("incomeCancelButton"),
  refreshIncomeButton: document.getElementById("refreshIncomeButton"),
  refreshCategoryButton: document.getElementById("refreshCategoryButton"),
  categorySummaryList: document.getElementById("categorySummaryList"),
  snapshotGrid: document.getElementById("snapshotGrid"),
  // Expense UI
  expenseTableBody: document.getElementById("expenseTableBody"),
  expenseForm: document.getElementById("expenseForm"),
  expenseId: document.getElementById("expenseId"),
  expenseAmount: document.getElementById("expenseAmount"),
  expenseCategoryId: document.getElementById("expenseCategoryId"),
  expenseDescription: document.getElementById("expenseDescription"),
  expenseSubmitButton: document.getElementById("expenseSubmitButton"),
  expenseCancelButton: document.getElementById("expenseCancelButton"),
  refreshExpenseButton: document.getElementById("refreshExpenseButton"),
};
function renderExpenseCategoryOptions() {
  elements.expenseCategoryId.innerHTML =
    '<option value="">Select category</option>' +
    state.categories
      .map((cat) => `<option value="${cat.id}">${cat.name}</option>`)
      .join("");
}

function renderExpenseTable() {
  if (!state.expenses.length) {
    elements.expenseTableBody.innerHTML =
      '<tr><td colspan="4" class="empty-state">No expenses have been saved yet.</td></tr>';
    return;
  }
  const categoryMap = new Map(state.categories.map((c) => [c.id, c.name]));
  elements.expenseTableBody.innerHTML = state.expenses
    .map(
      (expense) => `
        <tr>
            <td>${formatAmount(expense.amount)}</td>
            <td>${categoryMap.get(expense.categoryId) || "Unknown"}</td>
            <td>${expense.description || ""}</td>
            <td>
                <div class="row-actions">
                    <button class="row-action" type="button" data-action="edit-expense" data-id="${expense.id}">Edit</button>
                    <button class="row-action" type="button" data-action="delete-expense" data-id="${expense.id}" data-tone="danger">Delete</button>
                </div>
            </td>
        </tr>
    `,
    )
    .join("");
}

function resetExpenseForm() {
  state.editingExpenseId = null;
  elements.expenseId.value = "";
  elements.expenseAmount.value = "";
  elements.expenseCategoryId.value = "";
  elements.expenseDescription.value = "";
  elements.expenseSubmitButton.textContent = "Save expense";
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
  elements.expenseCategoryId.value = String(expense.categoryId ?? "");
  elements.expenseDescription.value = expense.description || "";
  elements.expenseSubmitButton.textContent = "Update expense";
  elements.expenseCancelButton.hidden = false;
  elements.expenseAmount.focus();
}

async function loadExpenses() {
  state.expenses = await requestJson("/expenses");
  renderExpenseTable();
}

async function handleExpenseSubmit(event) {
  event.preventDefault();
  const amount = Number.parseInt(elements.expenseAmount.value, 10);
  const categoryId = Number.parseInt(elements.expenseCategoryId.value, 10);
  const description = elements.expenseDescription.value.trim();
  if (!Number.isInteger(amount) || amount <= 0) {
    setStatus("Expense must be a positive whole number.", "error");
    return;
  }
  if (!Number.isInteger(categoryId) || categoryId <= 0) {
    setStatus("Please select a valid category.", "error");
    return;
  }
  const payload = { amount, categoryId, description };
  try {
    if (state.editingExpenseId == null) {
      await requestJson("/expenses", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setStatus("Expense added successfully.", "success");
    } else {
      await requestJson(`/expenses/${state.editingExpenseId}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      setStatus("Expense updated successfully.", "success");
    }
    resetExpenseForm();
    await refreshDashboard();
    await loadExpenses();
  } catch (error) {
    setStatus(error.message, "error");
  }
}

async function handleExpenseTableClick(event) {
  const button = event.target.closest("button[data-action]");
  if (!button) {
    return;
  }
  const expenseId = Number(button.dataset.id);
  const action = button.dataset.action;
  try {
    if (action === "edit-expense") {
      beginExpenseEdit(expenseId);
      return;
    }
    if (action === "delete-expense") {
      const confirmed = window.confirm(`Delete expense #${expenseId}?`);
      if (!confirmed) {
        return;
      }
      await requestJson(`/expenses/${expenseId}`, { method: "DELETE" });
      if (state.editingExpenseId === expenseId) {
        resetExpenseForm();
      }
      await refreshDashboard("Expense removed successfully.");
      await loadExpenses();
    }
  } catch (error) {
    setStatus(error.message, "error");
  }
}

const currencyFormatter = new Intl.NumberFormat("sv-SE");

async function requestJson(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
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

function renderHeroStats() {
  const totalIncome = state.incomes.reduce(
    (sum, income) => sum + Number(income.amount || 0),
    0,
  );
  const totalCategorySpend = state.summaries.reduce(
    (sum, item) => sum + Number(item.totalExpense || 0),
    0,
  );
  const remaining = totalIncome - totalCategorySpend;

  elements.heroStats.innerHTML = `
        <article class="stat-card">
            <span class="stat-label">Stored income</span>
            <div class="stat-value">${formatAmount(totalIncome)}</div>
        </article>
        <article class="stat-card">
            <span class="stat-label">Budget remainder</span>
            <div class="stat-value">${formatAmount(remaining)}</div>
        </article>
    `;
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
            <td>${formatAmount(income.amount)}</td>
            <td>
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
    elements.categorySummaryList.innerHTML =
      '<div class="empty-state">No categories are available yet.</div>';
    return;
  }

  const summaryByCategoryId = new Map(
    state.summaries.map((summary) => [summary.categoryId, summary]),
  );

  elements.categorySummaryList.innerHTML = state.categories
    .map((category) => {
      const summary = summaryByCategoryId.get(category.id);
      const spent = summary ? Number(summary.totalExpense || 0) : 0;
      const limit =
        category.categoryLimit == null ? null : Number(category.categoryLimit);
      const remaining = limit == null ? null : limit - spent;

      return `
            <article class="category-card">
                <h3>${category.name}</h3>
                <div class="category-meta">${category.description || "No description provided."}</div>
                <div class="category-figure">
                    <span>Spent</span>
                    <strong>${formatAmount(spent)}</strong>
                </div>
                <div class="category-figure">
                    <span>Limit</span>
                    <strong>${limit == null ? "Not set" : formatAmount(limit)}</strong>
                </div>
                <div class="category-figure">
                    <span>Remaining</span>
                    <strong>${remaining == null ? "Not set" : formatAmount(remaining)}</strong>
                </div>
            </article>
        `;
    })
    .join("");
}

function renderSnapshot() {
  const categoryCount = state.categories.length;
  const incomeCount = state.incomes.length;
  const totalCategorySpend = state.summaries.reduce(
    (sum, item) => sum + Number(item.totalExpense || 0),
    0,
  );

  elements.snapshotGrid.innerHTML = `
        <article class="snapshot-card">
            <span class="metric-label">Category spend</span>
            <div class="metric-value">${formatAmount(totalCategorySpend)}</div>
            <p class="snapshot-note">Summed from the category summary endpoint.</p>
        </article>
    `;
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
  renderSnapshot();
}

async function loadCategories() {
  const [categories, summaries] = await Promise.all([
    requestJson("/categories"),
    requestJson("/categories/summaries"),
  ]);

  state.categories = categories;
  state.summaries = summaries;
  renderCategories();
  renderHeroStats();
  renderSnapshot();
}

async function refreshDashboard(message = "Data refreshed.") {
  await Promise.all([loadIncome(), loadCategories(), loadExpenses()]);
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
      const confirmed = window.confirm(`Delete income #${incomeId}?`);
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
  elements.incomeForm.addEventListener("submit", handleIncomeSubmit);
  elements.incomeTableBody.addEventListener("click", handleIncomeTableClick);
  elements.refreshIncomeButton.addEventListener("click", () =>
    refreshDashboard("Income refreshed."),
  );
  elements.refreshCategoryButton.addEventListener("click", () =>
    refreshDashboard("Category data refreshed."),
  );
  elements.incomeCancelButton.addEventListener("click", () => {
    resetIncomeForm();
    setStatus("Income edit cancelled.", "success");
  });

  // Expense UI events
  elements.expenseForm.addEventListener("submit", handleExpenseSubmit);
  elements.expenseTableBody.addEventListener("click", handleExpenseTableClick);
  elements.refreshExpenseButton.addEventListener("click", () =>
    refreshDashboard("Expense data refreshed."),
  );
  elements.expenseCancelButton.addEventListener("click", () => {
    resetExpenseForm();
    setStatus("Expense edit cancelled.", "success");
  });

  try {
    await refreshDashboard("Dashboard loaded.");
    renderExpenseCategoryOptions();
  } catch (error) {
    setStatus(error.message, "error");
  }
}

document.addEventListener("DOMContentLoaded", start);
