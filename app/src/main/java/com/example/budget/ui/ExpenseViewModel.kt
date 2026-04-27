package com.example.budget.ui

import androidx.lifecycle.*
import com.example.budget.data.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions.asLiveData()
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()
    val expenseCategorySummaries: LiveData<List<CategorySummary>> = repository.expenseCategorySummaries.asLiveData()
    val incomeCategorySummaries: LiveData<List<CategorySummary>> = repository.incomeCategorySummaries.asLiveData()
    
    val totalExpenses: LiveData<Double?> = repository.totalExpenses.asLiveData()
    val totalIncome: LiveData<Double?> = repository.totalIncome.asLiveData()

    val balance: MediatorLiveData<Double> = MediatorLiveData<Double>().apply {
        addSource(totalExpenses) { value = calculateBalance() }
        addSource(totalIncome) { value = calculateBalance() }
    }

    private fun calculateBalance(): Double {
        val income = totalIncome.value ?: 0.0
        val expenses = totalExpenses.value ?: 0.0
        return income - expenses
    }

    fun getCategoriesByType(type: TransactionType): LiveData<List<Category>> = 
        repository.getCategoriesByType(type).asLiveData()

    // Transaction operations
    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    // Category operations
    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    // Budget operations
    fun getBudgetsForCurrentMonth(): LiveData<List<Budget>> {
        val cal = Calendar.getInstance()
        return repository.getBudgetsForMonth(
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        ).asLiveData()
    }

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun updateBudget(budget: Budget) = viewModelScope.launch {
        repository.updateBudget(budget)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    class ExpenseViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
