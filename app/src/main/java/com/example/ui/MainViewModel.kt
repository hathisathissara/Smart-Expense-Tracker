package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.firebase.FirebaseService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = DatabaseRepository(db.databaseDao())
    val firebaseService = FirebaseService(application)

    // Current logged-in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // App configurations
    private val _themeSetting = MutableStateFlow("SYSTEM") // "SYSTEM", "LIGHT", "DARK"
    val themeSetting: StateFlow<String> = _themeSetting.asStateFlow()

    private val _pinLocked = MutableStateFlow(false)
    val pinLocked: StateFlow<Boolean> = _pinLocked.asStateFlow()

    private val _pinCode = MutableStateFlow("")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    // Sync variables
    private val _syncStatus = MutableStateFlow("IDLE") // "IDLE", "SYNCING", "SUCCESS", "ERROR"
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    // Lists backed by Flow
    val transactions: StateFlow<List<TransactionEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getTransactionsForUser(user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getCategoriesForUser(user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getBudgetsForUser(user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) repository.getNotificationsForUser(user.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate standard categories
        viewModelScope.launch {
            repository.getCategoriesForUserDirect(0).let { existing ->
                if (existing.isEmpty()) {
                    val defaults = listOf(
                        CategoryEntity(0, 0, "Food & Dining", "EXPENSE", "Fastfood", "#FF9800"),
                        CategoryEntity(0, 0, "Shopping", "EXPENSE", "ShoppingBag", "#E91E63"),
                        CategoryEntity(0, 0, "Transport", "EXPENSE", "DirectionsCar", "#2196F3"),
                        CategoryEntity(0, 0, "Rent & Utilities", "EXPENSE", "Home", "#9C27B0"),
                        CategoryEntity(0, 0, "Entertainment", "EXPENSE", "LocalActivity", "#4CAF50"),
                        CategoryEntity(0, 0, "Healthcare", "EXPENSE", "MedicalServices", "#F44336"),
                        CategoryEntity(0, 0, "Education", "EXPENSE", "School", "#3F51B5"),
                        CategoryEntity(0, 0, "Other", "EXPENSE", "Category", "#607D8B"),
                        CategoryEntity(0, 0, "Salary", "INCOME", "Payments", "#4CAF50"),
                        CategoryEntity(0, 0, "Freelance & Side Hustles", "INCOME", "LaptopMac", "#00BCD4"),
                        CategoryEntity(0, 0, "Investments", "INCOME", "TrendingUp", "#9C27B0"),
                        CategoryEntity(0, 0, "Gifts", "INCOME", "CardGiftcard", "#FFC107")
                    )
                    defaults.forEach { repository.insertCategory(it) }
                }
            }
        }
    }

    // --- Authentication ---
    fun registerUser(name: String, email: String, password: CharArray, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val existing = repository.getUserByEmail(email)
                if (existing != null) {
                    onResult(false, "An account with this email already exists.")
                    return@launch
                }

                // Insert user locally
                val user = UserEntity(
                    email = email,
                    password = String(password),
                    name = name
                )
                val userId = repository.insertUser(user)

                // Try registering with real Firebase in background if online
                if (firebaseService.isFirebaseAvailable) {
                    firebaseService.signUpWithFirebase(email, password)
                }

                val savedUser = repository.getUserByIdDirect(userId)
                _currentUser.value = savedUser
                onResult(true, "Registration successful!")
            } catch (e: Exception) {
                onResult(false, "Registration failed: ${e.message}")
            }
        }
    }

    fun loginUser(email: String, password: CharArray, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // Try logging in locally first
                val localUser = repository.getUserByEmail(email)
                if (localUser != null && localUser.password == String(password)) {
                    _currentUser.value = localUser
                    
                    // Attempt real Firebase sign-in in background if available
                    if (firebaseService.isFirebaseAvailable) {
                        firebaseService.signInWithFirebase(email, password)
                    }
                    onResult(true, "Login successful!")
                    return@launch
                }

                // If local check fails, let's see if we can do real Firebase check
                if (firebaseService.isFirebaseAvailable) {
                    val firebaseSuccess = firebaseService.signInWithFirebase(email, password)
                    if (firebaseSuccess) {
                        // Create a local shadow user profile if it didn't exist locally
                        val firebaseEmail = firebaseService.auth?.currentUser?.email ?: email
                        val firebaseName = firebaseService.auth?.currentUser?.displayName ?: "Firebase User"
                        val newUser = UserEntity(
                            email = firebaseEmail,
                            password = String(password),
                            name = firebaseName
                        )
                        val id = repository.insertUser(newUser)
                        _currentUser.value = repository.getUserByIdDirect(id)
                        onResult(true, "Login successful via Firebase!")
                        return@launch
                    }
                }

                onResult(false, "Invalid email or password.")
            } catch (e: Exception) {
                onResult(false, "Login failed: ${e.message}")
            }
        }
    }

    fun loginWithGoogle(email: String, name: String, idToken: String?, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // If real idToken is passed and firebase is available, log in
                if (idToken != null && firebaseService.isFirebaseAvailable) {
                    firebaseService.signInWithGoogle(idToken)
                }

                // Retrieve local user or insert a new one
                var localUser = repository.getUserByEmail(email)
                if (localUser == null) {
                    // Create a local shadow profile with a randomized secure password stub
                    val newUser = UserEntity(
                        email = email,
                        password = "GoogleOAuthUser_" + java.util.UUID.randomUUID().toString().take(12),
                        name = name
                    )
                    val id = repository.insertUser(newUser)
                    localUser = repository.getUserByIdDirect(id)
                }

                _currentUser.value = localUser
                onResult(true, "Google Sign-In successful!")
            } catch (e: Exception) {
                onResult(false, "Google Sign-In failed: ${e.message}")
            }
        }
    }

    fun loginWithFacebook(email: String, name: String, accessToken: String?, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // If real accessToken is passed and firebase is available, log in
                if (accessToken != null && firebaseService.isFirebaseAvailable) {
                    firebaseService.signInWithFacebook(accessToken)
                }

                // Retrieve local user or insert a new one
                var localUser = repository.getUserByEmail(email)
                if (localUser == null) {
                    // Create a local shadow profile with a randomized secure password stub
                    val newUser = UserEntity(
                        email = email,
                        password = "FacebookOAuthUser_" + java.util.UUID.randomUUID().toString().take(12),
                        name = name
                    )
                    val id = repository.insertUser(newUser)
                    localUser = repository.getUserByIdDirect(id)
                }

                _currentUser.value = localUser
                onResult(true, "Facebook Sign-In successful!")
            } catch (e: Exception) {
                onResult(false, "Facebook Sign-In failed: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _currentUser.value = null
            if (firebaseService.isFirebaseAvailable) {
                firebaseService.auth?.signOut()
            }
        }
    }

    // --- Profile Editing & Settings ---
    fun updateProfile(name: String, currency: String, target: Double) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updatedUser = user.copy(
                name = name,
                preferredCurrency = currency,
                monthlyIncomeTarget = target
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun updateTheme(theme: String) {
        _themeSetting.value = theme
    }

    fun setSecurityPin(pin: String) {
        _pinCode.value = pin
        _pinLocked.value = pin.isNotEmpty()
    }

    fun unlockPin(pin: String): Boolean {
        return if (pin == _pinCode.value) {
            true
        } else {
            false
        }
    }

    // --- Transactions Management ---
    fun addTransaction(
        type: String, // "INCOME" or "EXPENSE"
        amount: Double,
        category: String,
        date: Long,
        description: String,
        tags: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val transaction = TransactionEntity(
                userId = user.id,
                type = type,
                amount = amount,
                category = category,
                date = date,
                description = description,
                tags = tags
            )
            
            repository.insertTransaction(transaction)

            // Budget Exceedance Alert System
            if (type == "EXPENSE") {
                checkBudgetForCategory(user.id, category, date, amount)
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // --- Category Management ---
    fun addCategory(name: String, type: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val category = CategoryEntity(
                userId = user.id,
                name = name,
                type = type,
                iconName = iconName,
                colorHex = colorHex
            )
            repository.insertCategory(category)
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    // --- Budget Management ---
    fun addBudget(categoryName: String, monthlyLimit: Double, monthYear: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val existing = repository.getBudgetForCategory(user.id, categoryName, monthYear)
            val budget = if (existing != null) {
                existing.copy(monthlyLimit = monthlyLimit)
            } else {
                BudgetEntity(
                    userId = user.id,
                    categoryName = categoryName,
                    monthlyLimit = monthlyLimit,
                    monthYear = monthYear
                )
            }
            repository.insertBudget(budget)
            
            // Re-trigger warnings on new budgets
            triggerBudgetReview(user.id, categoryName, monthYear, monthlyLimit)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    // --- Notification management ---
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            repository.clearAllNotifications(user.id)
        }
    }

    // --- Smart Budget Checker Logic ---
    private suspend fun checkBudgetForCategory(userId: Long, category: String, date: Long, addedAmount: Double) {
        val sdf = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        val monthYear = sdf.format(Date(date))

        val budget = repository.getBudgetForCategory(userId, category, monthYear) ?: return
        
        // Sum expenses in this category for this month
        val allTx = repository.getTransactionsForUserDirect(userId)
        val categoryExpenses = allTx.filter {
            it.type == "EXPENSE" &&
            it.category == category &&
            sdf.format(Date(it.date)) == monthYear
        }.sumOf { it.amount }

        if (categoryExpenses > budget.monthlyLimit) {
            val currency = _currentUser.value?.preferredCurrency ?: "$"
            val title = "Budget Alert: $category"
            val message = "You have spent ${currency}${String.format("%.2f", categoryExpenses)} which exceeds your monthly limit of ${currency}${String.format("%.2f", budget.monthlyLimit)}."
            
            repository.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = "BUDGET_ALERT"
                )
            )
        }
    }

    private suspend fun triggerBudgetReview(userId: Long, category: String, monthYear: String, limit: Double) {
        val sdf = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        val allTx = repository.getTransactionsForUserDirect(userId)
        val categoryExpenses = allTx.filter {
            it.type == "EXPENSE" &&
            it.category == category &&
            sdf.format(Date(it.date)) == monthYear
        }.sumOf { it.amount }

        if (categoryExpenses > limit) {
            val currency = _currentUser.value?.preferredCurrency ?: "$"
            val title = "Budget Limit Exceeded"
            val message = "Your expenses in $category for $monthYear have reached ${currency}${String.format("%.2f", categoryExpenses)}, exceeding your budget of ${currency}${String.format("%.2f", limit)}."
            repository.insertNotification(
                NotificationEntity(
                    userId = userId,
                    title = title,
                    message = message,
                    timestamp = System.currentTimeMillis(),
                    type = "BUDGET_ALERT"
                )
            )
        }
    }

    // --- Firebase Cloud Sync Backup & Restore Mock/Real ---
    fun backupDataToCloud() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _syncStatus.value = "SYNCING"
            _syncProgress.value = 0.1f
            try {
                val transactionsList = repository.getTransactionsForUserDirect(user.id)
                val budgetsList = repository.getBudgetsForUserDirect(user.id)
                val categoriesList = repository.getCategoriesForUserDirect(user.id)

                val backupData = mapOf(
                    "user_id" to user.id,
                    "email" to user.email,
                    "name" to user.name,
                    "preferredCurrency" to user.preferredCurrency,
                    "monthlyIncomeTarget" to user.monthlyIncomeTarget,
                    "transactions" to transactionsList.map {
                        mapOf(
                            "type" to it.type,
                            "amount" to it.amount,
                            "category" to it.category,
                            "date" to it.date,
                            "description" to it.description,
                            "tags" to it.tags
                        )
                    },
                    "budgets" to budgetsList.map {
                        mapOf(
                            "categoryName" to it.categoryName,
                            "monthlyLimit" to it.monthlyLimit,
                            "monthYear" to it.monthYear
                        )
                    },
                    "categories" to categoriesList.filter { it.userId != 0L }.map {
                        mapOf(
                            "name" to it.name,
                            "type" to it.type,
                            "iconName" to it.iconName,
                            "colorHex" to it.colorHex
                        )
                    },
                    "timestamp" to System.currentTimeMillis()
                )

                _syncProgress.value = 0.5f

                if (firebaseService.isFirebaseAvailable) {
                    val firestoreSuccess = firebaseService.syncDataToCloud(user.email, backupData)
                    if (firestoreSuccess) {
                        _syncProgress.value = 1.0f
                        _syncStatus.value = "SUCCESS"
                        repository.insertNotification(
                            NotificationEntity(
                                userId = user.id,
                                title = "Cloud Sync Success",
                                message = "All your expense logs, categories, and budget caps have been secured on Cloud Firestore.",
                                type = "MILESTONE"
                            )
                        )
                    } else {
                        throw Exception("Firestore upload error.")
                    }
                } else {
                    // Sandbox Mode / Fallback Mock Sync Simulation
                    kotlinx.coroutines.delay(2000)
                    _syncProgress.value = 0.8f
                    kotlinx.coroutines.delay(1000)
                    _syncProgress.value = 1.0f
                    _syncStatus.value = "SUCCESS"
                    
                    repository.insertNotification(
                        NotificationEntity(
                            userId = user.id,
                            title = "Local Archive Synced",
                            message = "Secure backup compiled! (Running in Sandbox mode since google-services.json is pending). Your database is fully intact.",
                            type = "MILESTONE"
                        )
                    )
                }
            } catch (e: Exception) {
                _syncStatus.value = "ERROR"
                _syncProgress.value = 0.0f
            }
        }
    }

    fun resetSyncStatus() {
        _syncStatus.value = "IDLE"
        _syncProgress.value = 0f
    }
}
