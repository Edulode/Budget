package com.example.budget.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT category, SUM(amount) as amount FROM transactions WHERE type = 'EXPENSE' GROUP BY category")
    fun getExpenseCategorySummaries(): Flow<List<CategorySummary>>

    @Query("SELECT category, SUM(amount) as amount FROM transactions WHERE type = 'INCOME' GROUP BY category")
    fun getIncomeCategorySummaries(): Flow<List<CategorySummary>>
}
