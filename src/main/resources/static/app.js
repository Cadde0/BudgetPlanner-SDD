const state = {
  incomes: [],
  categories: [],
  summaries: [],
  expenses: [],
  editingIncomeId: null,
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
  // Expense category assignment UI
  expenseTableBody: document.getElementById("expenseTableBody"),
  refreshExpenseButton: document.getElementById("refreshExpenseButton"),
};

function renderExpenseTable() {
  if (!state.expenses.length) {
    elements.expenseTableBody.innerHTML =
      '<tr><td colspan="4" class="empty-state">No expenses were found.</td></tr>';
    return;
  }

  const categoryOptions = state.categories
    .map((cat) => `<option value="${cat.id}">${cat.name}</option>`)
    .join("");

  elements.expenseTableBody.innerHTML = state.expenses
    .map(
      (expense) => `
        <tr>
            <td>${expense.description || ""}</td>
            <td>${formatAmount(expense.amount)}</td>
            <td>
              <select class="row-category-select" data-id="${expense.id}">
                <option value="">Select category</option>
                ${categoryOptions}
              </select>
            </td>
            <td>
              <button class="row-action" type="button" data-action="assign-category" data-id="${expense.id}">Assign</button>
            </td>
        </tr>
    `,
    )
    .join("");

  state.expenses.forEach((expense) => {
    const select = elements.expenseTableBody.querySelector(
      `select[data-id="${expense.id}"]`,
    );
    if (select && expense.categoryId != null) {
      select.value = String(expense.categoryId);
    }
  });
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
    if (action !== "assign-category") {
      return;
    }

    const select = elements.expenseTableBody.querySelector(
      `select[data-id="${expenseId}"]`,
    );
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
  renderExpenseTable();
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

  // Expense category assignment events
  elements.expenseTableBody.addEventListener("click", handleExpenseTableClick);
  elements.refreshExpenseButton.addEventListener("click", () =>
    refreshDashboard("Expense data refreshed."),
  );

  try {
    await refreshDashboard("Dashboard loaded.");
  } catch (error) {
    setStatus(error.message, "error");
  }
}

document.addEventListener("DOMContentLoaded", start);
