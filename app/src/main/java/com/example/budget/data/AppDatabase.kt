package com.example.budget.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Category::class, Budget::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val categoryDao = database.categoryDao()
                    if (categoryDao.getCategoryCount() == 0) {
                        populateCategories(categoryDao)
                    }
                }
            }
        }

        private suspend fun populateCategories(categoryDao: CategoryDao) {
            val expenseCategories = listOf(
                "Alimentación", "Transporte", "Vivienda", "Servicios", 
                "Salud", "Diversión", "Educación", "Suscripciones", 
                "Ropa", "Viajes", "Regalos", "Otros"
            )
            val incomeCategories = listOf(
                "Sueldo", "Freelance", "Inversiones", "Ventas", "Premios", "Otros"
            )
            
            expenseCategories.forEach {
                categoryDao.insert(Category(name = it, type = TransactionType.EXPENSE))
            }
            incomeCategories.forEach {
                categoryDao.insert(Category(name = it, type = TransactionType.INCOME))
            }
        }
    }
}
