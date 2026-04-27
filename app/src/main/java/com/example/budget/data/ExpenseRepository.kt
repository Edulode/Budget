package com.example.budget.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.insert(expense) // REPLACE strategy
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.delete(expense)
    }

    fun getTotalByCategory(category: String): Flow<Double> {
        return expenseDao.getTotalByCategory(category)
    }

    val categorySummaries: Flow<List<CategorySummary>> = expenseDao.getCategorySummaries()

    // Category methods
    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }
}
