package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val date: Long, // Epoch timestamp (ms)
    val description: String,
    val tags: String = "", // Comma-separated tags e.g. "groceries,food"
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
