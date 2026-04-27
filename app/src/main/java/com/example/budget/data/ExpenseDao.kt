package com.example.budget.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    fun getTotalByCategory(category: String): Flow<Double>

    @Query("SELECT category, SUM(amount) as amount FROM expenses GROUP BY category")
    fun getCategorySummaries(): Flow<List<CategorySummary>>
}

data class CategorySummary(
    val category: String,
    val amount: Double
)
