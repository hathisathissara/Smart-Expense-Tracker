package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    
    fun getIconForName(name: String): ImageVector {
        return when (name) {
            "Fastfood" -> Icons.Default.Fastfood
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Home" -> Icons.Default.Home
            "LocalActivity" -> Icons.Default.LocalActivity
            "MedicalServices" -> Icons.Default.MedicalServices
            "School" -> Icons.Default.School
            "Category" -> Icons.Default.Category
            "Payments" -> Icons.Default.Payments
            "LaptopMac" -> Icons.Default.LaptopMac
            "TrendingUp" -> Icons.Default.TrendingUp
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "Person" -> Icons.Default.Person
            "Lock" -> Icons.Default.Lock
            "Settings" -> Icons.Default.Settings
            "Analytics" -> Icons.Default.Analytics
            "Notifications" -> Icons.Default.Notifications
            "Sync" -> Icons.Default.Sync
            else -> Icons.Default.Payments
        }
    }

    fun getColorFromHex(hex: String, default: Color = Color(0xFF6200EE)): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            default
        }
    }
}
