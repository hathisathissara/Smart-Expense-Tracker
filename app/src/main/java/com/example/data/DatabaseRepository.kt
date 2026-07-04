package com.example.data

import kotlinx.coroutines.flow.Flow

class DatabaseRepository(private val dao: DatabaseDao) {

    // --- Users ---
    suspend fun insertUser(user: UserEntity): Long = dao.insertUser(user)
    
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
    
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    
    fun getUserById(id: Long): Flow<UserEntity?> = dao.getUserById(id)
    
    suspend fun getUserByIdDirect(id: Long): UserEntity? = dao.getUserByIdDirect(id)


    // --- Transactions ---
    suspend fun insertTransaction(transaction: TransactionEntity): Long = dao.insertTransaction(transaction)
    
    fun getTransactionsForUser(userId: Long): Flow<List<TransactionEntity>> = dao.getTransactionsForUser(userId)
    
    suspend fun getTransactionsForUserDirect(userId: Long): List<TransactionEntity> = dao.getTransactionsForUserDirect(userId)
    
    suspend fun deleteTransaction(id: Long) = dao.deleteTransaction(id)
    
    suspend fun clearTransactionsForUser(userId: Long) = dao.clearTransactionsForUser(userId)


    // --- Categories ---
    suspend fun insertCategory(category: CategoryEntity): Long = dao.insertCategory(category)
    
    fun getCategoriesForUser(userId: Long): Flow<List<CategoryEntity>> = dao.getCategoriesForUser(userId)
    
    suspend fun getCategoriesForUserDirect(userId: Long): List<CategoryEntity> = dao.getCategoriesForUserDirect(userId)
    
    suspend fun deleteCategory(id: Long) = dao.deleteCategory(id)


    // --- Budgets ---
    suspend fun insertBudget(budget: BudgetEntity): Long = dao.insertBudget(budget)
    
    fun getBudgetsForUser(userId: Long): Flow<List<BudgetEntity>> = dao.getBudgetsForUser(userId)
    
    suspend fun getBudgetsForUserDirect(userId: Long): List<BudgetEntity> = dao.getBudgetsForUserDirect(userId)
    
    suspend fun getBudgetForCategory(userId: Long, categoryName: String, monthYear: String): BudgetEntity? =
        dao.getBudgetForCategory(userId, categoryName, monthYear)
        
    suspend fun deleteBudget(id: Long) = dao.deleteBudget(id)


    // --- Notifications ---
    suspend fun insertNotification(notification: NotificationEntity): Long = dao.insertNotification(notification)
    
    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>> = dao.getNotificationsForUser(userId)
    
    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    
    suspend fun clearAllNotifications(userId: Long) = dao.clearAllNotifications(userId)
}
