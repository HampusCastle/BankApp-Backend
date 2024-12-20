package hampusborg.bankapp.application.service.base

import hampusborg.bankapp.application.dto.response.*
import hampusborg.bankapp.application.exception.classes.NoTransactionsFoundException
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.core.domain.*
import hampusborg.bankapp.core.repository.*
import hampusborg.bankapp.core.util.AccountUtils
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class CacheHelperService(
    private val cacheManager: CacheManager,
    private val accountRepository: AccountRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val scheduledPaymentRepository: ScheduledPaymentRepository
) {

    private val log = LoggerFactory.getLogger(CacheHelperService::class.java)

    private fun <T> getCache(cacheName: String, key: String, clazz: Class<T>): T? {
        val cache = cacheManager.getCache(cacheName)
        return cache?.get(key, clazz)
    }

    private fun <T> putCache(cacheName: String, key: String, value: T) {
        val cache = cacheManager.getCache(cacheName)
        cache?.put(key, value)
        println("Cache updated: $cacheName with key: $key, value: $value")
    }

    fun getUserFromCache(userId: String): User? {
        return getCache("userCache", userId, User::class.java)
    }

    fun storeUser(user: User) {
        putCache("userCache", user.id!!, user)
    }

    fun getSavingsGoal(id: String): SavingsGoal? {
        return getCache("savingsGoals", id, SavingsGoal::class.java)
    }

    fun storeSavingsGoal(id: String, savingsGoal: SavingsGoal) {
        putCache("savingsGoals", id, savingsGoal)
    }

    fun getSavingsGoalsByUserId(userId: String): List<SavingsGoal> {
        val cachedSavingsGoals = getCache("userSavingsGoals", userId, List::class.java)
        return if (cachedSavingsGoals is List<*>) {
            cachedSavingsGoals.filterIsInstance<SavingsGoal>()
        } else {
            loadSavingsGoalsByUserIdFromDbAndCache(userId)
        }
    }

    private fun loadSavingsGoalsByUserIdFromDbAndCache(userId: String): List<SavingsGoal> {
        val savingsGoals = savingsGoalRepository.findByUserId(userId)
        putCache("userSavingsGoals", userId, savingsGoals)
        return savingsGoals
    }

    fun getAccountsByUserId(userId: String): List<AccountDetailsResponse> {
        val cachedAccounts = getCache("userAccounts", userId, List::class.java)
        return if (cachedAccounts is List<*>) {
            cachedAccounts.filterIsInstance<AccountDetailsResponse>()
        } else {
            loadAccountsByUserIdFromDbAndCache(userId)
        }
    }

    private fun loadAccountsByUserIdFromDbAndCache(userId: String): List<AccountDetailsResponse> {
        val accounts = accountRepository.findByUserId(userId)
        val accountDetails = accounts.map { account ->
            AccountDetailsResponse(
                id = account.id ?: "",
                name = account.name,
                balance = account.balance,
                accountType = account.accountType.name,
                userId = account.userId
            )
        }
        putCache("userAccounts", userId, accountDetails)
        return accountDetails
    }

    fun getMonthlyExpenses(userId: String): ExpensesSummaryResponse {
        return getCache("monthlyExpenses", userId, ExpensesSummaryResponse::class.java)
            ?: loadMonthlyExpensesFromDbAndCache(userId)
    }

    fun cacheMonthlyExpenses(userId: String, expensesSummary: ExpensesSummaryResponse) {
        putCache("monthlyExpenses", userId, expensesSummary)
    }

    private fun loadMonthlyExpensesFromDbAndCache(userId: String): ExpensesSummaryResponse {
        val transactions = loadTransactionsByAccountIdAndUserId(userId, userId)

        if (transactions.isEmpty()) {
            throw NoTransactionsFoundException("No transactions found for user ID: $userId")
        }

        val expensesSummary = calculateExpensesSummary(transactions)
        cacheMonthlyExpenses(userId, expensesSummary)
        return expensesSummary
    }

    private fun loadTransactionsByAccountIdAndUserId(userId: String, accountId: String): List<Transaction> {
        log.info("Fetching transactions for userId: $userId and accountId: $accountId")

        val transactionsFromAccount = transactionRepository.findByFromAccountIdAndUserId(accountId, userId)
        val transactionsToAccount = transactionRepository.findByToAccountIdAndUserId(accountId, userId)

        log.debug("Found ${transactionsFromAccount.size} transactions from the account.")
        log.debug("Found ${transactionsToAccount.size} transactions to the account.")

        val transactions = transactionsFromAccount + transactionsToAccount
        if (transactions.isEmpty()) {
            log.warn("No transactions found for accountId: $accountId and userId: $userId")
        }

        return transactions
    }

    private fun calculateExpensesSummary(transactions: List<Transaction>): ExpensesSummaryResponse {
        val totalExpenses = transactions.sumOf { it.amount }
        val categories = transactions.groupBy { it.categoryId.name }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        log.debug("Calculated expenses: total = $totalExpenses, categories = $categories")

        return ExpensesSummaryResponse(totalExpenses, categories)
    }

    fun getSubscriptionById(id: String): Subscription {
        return getCache("subscriptions", id, Subscription::class.java)
            ?: loadSubscriptionFromDbAndCache(id)
    }

    private fun loadSubscriptionFromDbAndCache(id: String): Subscription {
        val subscription = subscriptionRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("Subscription not found for ID: $id")
        }
        putCache("subscriptions", id, subscription)
        return subscription
    }

    fun getSubscriptionsByUserId(userId: String): List<Subscription> {
        val cachedSubscriptions = getCache("userSubscriptions", userId, List::class.java)
        return if (cachedSubscriptions is List<*>) {
            cachedSubscriptions.filterIsInstance<Subscription>()
        } else {
            loadSubscriptionsByUserIdFromDbAndCache(userId)
        }
    }

    private fun loadSubscriptionsByUserIdFromDbAndCache(userId: String): List<Subscription> {
        val subscriptions = subscriptionRepository.findByUserId(userId)
        putCache("userSubscriptions", userId, subscriptions)
        return subscriptions
    }

    fun getUserByUsername(username: String): User {
        return getCache("userCache", username, User::class.java)
            ?: loadUserFromDbAndCache(username)
    }

    private fun loadUserFromDbAndCache(username: String): User {
        val user = userRepository.findByUsername(username) ?: throw UserNotFoundException("User not found with username: $username")
        putCache("userCache", username, user)
        return user
    }

    fun getTransactionHistory(userId: String): TransactionHistoryDetailsResponse {
        return getCache("transactionHistory", userId, TransactionHistoryDetailsResponse::class.java)
            ?: loadTransactionHistoryFromDbAndCache(userId)
    }

    private fun loadTransactionHistoryFromDbAndCache(userId: String): TransactionHistoryDetailsResponse {
        val transactions = transactionRepository.findByFromAccountIdAndUserId(userId, userId) +
                transactionRepository.findByToAccountIdAndUserId(userId, userId)

        val history = transactions.map {
            Pair(it.id ?: "", it.amount)
        }

        val response = TransactionHistoryDetailsResponse(
            history.joinToString("\n") { "Transaction ID: ${it.first}, Amount: ${it.second}" }
        )

        putCache("transactionHistory", userId, response)
        return response
    }

    fun getMarketTrends(userId: String): MarketTrendsDetailsResponse {
        return getCache("marketTrends", userId, MarketTrendsDetailsResponse::class.java)
            ?: loadMarketTrendsFromDbAndCache(userId)
    }

    private fun loadMarketTrendsFromDbAndCache(userId: String): MarketTrendsDetailsResponse {
        val response = MarketTrendsDetailsResponse(
            trend = "Upward",
            price = "5000 SEK",
            volume = "3000 units",
            changePercent = "5%"
        )
        putCache("marketTrends", userId, response)
        return response
    }

    fun getScheduledPayments(userId: String): ScheduledPaymentDetailsResponse {
        return getCache("scheduledPayments", userId, ScheduledPaymentDetailsResponse::class.java)
            ?: loadScheduledPaymentsFromDbAndCache(userId)
    }

    private fun loadScheduledPaymentsFromDbAndCache(userId: String): ScheduledPaymentDetailsResponse {
        val payments = scheduledPaymentRepository.findByUserId(userId)
        val response = ScheduledPaymentDetailsResponse(payments.joinToString("\n") { "Scheduled Payment ID: ${it.id}, Amount: ${it.amount}" })
        putCache("scheduledPayments", userId, response)
        return response
    }

    fun getAccountInfo(userId: String): String {
        return getCache("accountInfo", userId, String::class.java)
            ?: loadAccountInfoFromDbAndCache(userId)
    }

    fun storeAccountsByUserId(userId: String, accounts: List<Account>) {
        val currentAccounts = getCache("userAccounts", userId, List::class.java) as? List<AccountDetailsResponse> ?: emptyList()

        val updatedAccounts = (currentAccounts + accounts.map { AccountUtils.mapToAccountDetailsResponse(it) }).toList()

        putCache("userAccounts", userId, updatedAccounts)
    }

    private fun loadAccountInfoFromDbAndCache(userId: String): String {
        val accounts = accountRepository.findByUserId(userId)
        val info = "User has ${accounts.size} accounts"
        putCache("accountInfo", userId, info)
        return info
    }

    fun getFinancialNews(): List<ExternalApiNewsHandler.FinancialNewsDetailsResponse> {
        val cachedNews = getCache("financialNews", "allNews", List::class.java)
        return if (cachedNews is List<*>) {
            cachedNews.filterIsInstance<ExternalApiNewsHandler.FinancialNewsDetailsResponse>()
        } else {
            emptyList()
        }
    }

    fun storeFinancialNews(news: List<ExternalApiNewsHandler.FinancialNewsDetailsResponse>) {
        putCache("financialNews", "allNews", news)
    }

    fun evictCache(cacheName: String, key: String) {
        val cache = cacheManager.getCache(cacheName)
        cache?.evict(key)
    }
}