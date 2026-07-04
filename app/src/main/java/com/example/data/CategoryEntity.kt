package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long, // 0 for default globally available categories
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String, // Material symbol descriptor
    val colorHex: String // Color configuration e.g. "#FF9800"
)
