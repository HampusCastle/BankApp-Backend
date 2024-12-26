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
        log.debug("Cache updated: $cacheName with key: $key, value: $value")
    }


    fun getMonthlyExpenses(userId: String): ExpensesSummaryResponse? {
        return getCache("monthlyExpenses", userId, ExpensesSummaryResponse::class.java)
    }

    fun cacheMonthlyExpenses(userId: String, expensesSummary: ExpensesSummaryResponse) {
        putCache("monthlyExpenses", userId, expensesSummary)
    }

    fun getUserFromCache(userId: String): User? {
        return getCache("userCache", userId, User::class.java)
    }

    fun storeUser(user: User) {
        putCache("userCache", user.id!!, user)
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
        val subscriptions = subscriptionRepository.findAllByUserId(userId)
        putCache("userSubscriptions", userId, subscriptions)
        return subscriptions
    }

    private fun loadSavingsGoalsByUserIdFromDbAndCache(userId: String): List<SavingsGoal> {
        val savingsGoals = savingsGoalRepository.findByUserId(userId)
        putCache("userSavingsGoals", userId, savingsGoals)
        return savingsGoals
    }

    fun getAccountsByUserId(userId: String): List<AccountDetailsResponse> {
        return getCache("userAccounts", userId, List::class.java)
            ?.filterIsInstance<AccountDetailsResponse>()
            ?: loadAccountsByUserIdFromDbAndCache(userId)
    }

    private fun loadAccountsByUserIdFromDbAndCache(userId: String): List<AccountDetailsResponse> {
        val accounts = accountRepository.findByUserId(userId)
        val accountDetails = accounts.map { AccountUtils.mapToAccountDetailsResponse(it) }
        putCache("userAccounts", userId, accountDetails)
        return accountDetails
    }

    fun storeAccountsByUserId(userId: String, accounts: List<Account>) {
        val accountDetails = accounts.map { AccountUtils.mapToAccountDetailsResponse(it) }
        putCache("userAccounts", userId, accountDetails)
    }

    fun getTransactionHistory(userId: String): TransactionHistoryDetailsResponse {
        return getCache("transactionHistory", userId, TransactionHistoryDetailsResponse::class.java)
            ?: loadTransactionHistoryFromDbAndCache(userId)
    }

    private fun loadTransactionHistoryFromDbAndCache(userId: String): TransactionHistoryDetailsResponse {
        val transactions = transactionRepository.findByUserId(userId)
        if (transactions.isEmpty()) throw NoTransactionsFoundException("No transactions found for user ID: $userId")

        val history = transactions.map { Pair(it.id ?: "", it.amount) }
        val response = TransactionHistoryDetailsResponse(
            history = history.joinToString("\n") { "Transaction ID: ${it.first}, Amount: ${it.second}" }
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
        val response = ScheduledPaymentDetailsResponse(
            message = payments.joinToString("\n") { "Scheduled Payment ID: ${it.id}, Amount: ${it.amount}" }
        )
        putCache("scheduledPayments", userId, response)
        return response
    }

    fun getFinancialNews(): List<ExternalApiNewsHandler.FinancialNewsDetailsResponse> {
        return getCache("financialNews", "allNews", List::class.java)
            ?.filterIsInstance<ExternalApiNewsHandler.FinancialNewsDetailsResponse>()
            ?: emptyList()
    }

    fun storeFinancialNews(news: List<ExternalApiNewsHandler.FinancialNewsDetailsResponse>) {
        putCache("financialNews", "allNews", news)
    }

    fun storeSavingsGoal(id: String, savingsGoal: SavingsGoal) {
        putCache("savingsGoals", id, savingsGoal)
    }

    fun evictAndStoreAllAccountsForUser(userId: String) {
        evictCache("userAccounts", userId)
        val accounts = accountRepository.findByUserId(userId)
        storeAccountsByUserId(userId, accounts)
    }


    fun evictCache(cacheName: String, key: String) {
        cacheManager.getCache(cacheName)?.evict(key)
    }
}