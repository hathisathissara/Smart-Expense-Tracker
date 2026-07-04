package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val categoryName: String,
    val monthlyLimit: Double,
    val monthYear: String // Format "MM-yyyy" e.g. "07-2026"
)
