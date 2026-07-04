package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currency = user?.preferredCurrency ?: "$"

    val sdf = remember { SimpleDateFormat("MM-yyyy", Locale.getDefault()) }
    val currentMonthStr = remember { sdf.format(Date()) }

    // Expenses of the current month
    val expenses = remember(transactions) {
        transactions.filter {
            it.type == "EXPENSE" && sdf.format(Date(it.date)) == currentMonthStr
        }
    }

    val totalExpense = remember(expenses) { expenses.sumOf { it.amount } }

    // Group expenses by category
    val categoryExpenses = remember(expenses) {
        expenses.groupBy { it.category }.map { (catName, txList) ->
            catName to txList.sumOf { it.amount }
        }.sortedByDescending { it.second }
    }

    // Daily expenses of current month (for line chart)
    val dailyExpenses = remember(expenses) {
        val daysMap = mutableMapOf<Int, Double>()
        // Initialize days 1-30/31
        val cal = Calendar.getInstance()
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDays) {
            daysMap[i] = 0.0
        }
        expenses.forEach { tx ->
            cal.timeInMillis = tx.date
            val day = cal.get(Calendar.DAY_OF_MONTH)
            daysMap[day] = (daysMap[day] ?: 0.0) + tx.amount
        }
        daysMap.toSortedMap().toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visual Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Outflow Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Detailed category ratios for the current month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Pie/Donut Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (totalExpense <= 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No expenses recorded this month.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Donut Chart drawing Canvas
                            Box(
                                modifier = Modifier
                                    .size(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val chartColors = listOf(
                                    Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF2196F3),
                                    Color(0xFF9C27B0), Color(0xFF4CAF50), Color(0xFFF44336),
                                    Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFFFFC107)
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    var startAngle = -90f
                                    categoryExpenses.forEachIndexed { index, (_, amount) ->
                                        val sweepAngle = ((amount / totalExpense) * 360f).toFloat()
                                        val color = chartColors[index % chartColors.size]
                                        
                                        // Draw the donut slice
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            size = Size(size.width, size.height),
                                            style = Stroke(width = 30.dp.toPx())
                                        )
                                        startAngle += sweepAngle
                                    }
                                }

                                // Center labels
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Total Spent",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${currency}${String.format("%,.0f", totalExpense)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Donut Chart legend
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val chartColors = listOf(
                                    Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF2196F3),
                                    Color(0xFF9C27B0), Color(0xFF4CAF50), Color(0xFFF44336),
                                    Color(0xFF3F51B5), Color(0xFF00BCD4), Color(0xFFFFC107)
                                )

                                categoryExpenses.forEachIndexed { index, (catName, amount) ->
                                    val pct = (amount / totalExpense) * 100
                                    val color = chartColors[index % chartColors.size]
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(color, CircleShape)
                                            )
                                            Text(
                                                text = catName,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            text = "${currency}${String.format("%.2f", amount)} (${String.format("%.1f", pct)}%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Outflow Trend Chart Card
            item {
                Text(
                    text = "Daily Trend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Cash flow progression across the calendar month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val maxDayAmount = remember(dailyExpenses) {
                            val max = dailyExpenses.maxOfOrNull { it.second } ?: 10.0
                            if (max == 0.0) 10.0 else max
                        }

                        if (totalExpense <= 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Waiting for expense data...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Line chart Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                val lineColor = MaterialTheme.colorScheme.primary
                                val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height
                                    val count = dailyExpenses.size
                                    
                                    val stepX = width / (count - 1).coerceAtLeast(1)
                                    
                                    val path = Path()
                                    val fillPath = Path()

                                    dailyExpenses.forEachIndexed { index, (_, value) ->
                                        // Coordinate calculation
                                        val x = index * stepX
                                        val y = height - ((value / maxDayAmount) * height * 0.85f).toFloat()

                                        if (index == 0) {
                                            path.moveTo(x, y)
                                            fillPath.moveTo(x, height)
                                            fillPath.lineTo(x, y)
                                        } else {
                                            // Draw smooth bezier curves
                                            val prevX = (index - 1) * stepX
                                            val prevY = height - ((dailyExpenses[index - 1].second / maxDayAmount) * height * 0.85f).toFloat()
                                            
                                            val controlX1 = prevX + stepX / 2f
                                            val controlY1 = prevY
                                            val controlX2 = prevX + stepX / 2f
                                            val controlY2 = y

                                            path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                                        }

                                        if (index == count - 1) {
                                            fillPath.lineTo(x, height)
                                            fillPath.close()
                                        }
                                    }

                                    // Draw the glow background
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(gradientColor, Color.Transparent)
                                        )
                                    )

                                    // Draw the solid line
                                    drawPath(
                                        path = path,
                                        color = lineColor,
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Day 1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Day ${dailyExpenses.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
