package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val password: String, // Securely checked locally for registration/login demo
    val name: String,
    val avatarUrl: String? = null,
    val preferredCurrency: String = "$",
    val monthlyIncomeTarget: Double = 3000.0,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
