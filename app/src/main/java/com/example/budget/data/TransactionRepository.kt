package com.example.budget.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> = 
        categoryDao.getCategoriesByType(type)

    val totalExpenses: Flow<Double?> = transactionDao.getTotalExpenses()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    val expenseCategorySummaries: Flow<List<CategorySummary>> = transactionDao.getExpenseCategorySummaries()
    val incomeCategorySummaries: Flow<List<CategorySummary>> = transactionDao.getIncomeCategorySummaries()

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

    // Budget methods
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(month, year)

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insert(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.update(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.delete(budget)
    }
}
