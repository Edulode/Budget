package com.example.budget

import android.app.Application
import com.example.budget.data.AppDatabase
import com.example.budget.data.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BudgetApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { 
        TransactionRepository(
            database.transactionDao(), 
            database.categoryDao(),
            database.budgetDao()
        ) 
    }
}
