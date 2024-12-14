package hampusborg.bankapp.application.service.base

import hampusborg.bankapp.application.dto.response.*
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.core.domain.*
import hampusborg.bankapp.core.repository.*
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

    fun getSavingsGoal(id: String): SavingsGoal? {
        val cache = cacheManager.getCache("savingsGoals")
        return cache?.get(id, SavingsGoal::class.java)
    }

    fun storeSavingsGoal(id: String, savingsGoal: SavingsGoal) {
        val cache = cacheManager.getCache("savingsGoals")
        cache?.put(id, savingsGoal)
    }

    fun getSavingsGoalsByUserId(userId: String): List<SavingsGoal> {
        val cache = cacheManager.getCache("userSavingsGoals")
        return cache?.get(userId, List::class.java)?.let {
            it as? List<SavingsGoal> ?: emptyList()
        } ?: loadSavingsGoalsByUserIdFromDbAndCache(userId)
    }

    private fun loadSavingsGoalsByUserIdFromDbAndCache(userId: String): List<SavingsGoal> {
        val savingsGoals = savingsGoalRepository.findAllByUserId(userId)
        cacheManager.getCache("userSavingsGoals")?.put(userId, savingsGoals)
        return savingsGoals
    }

    fun getAccountsByUserId(userId: String): List<AccountDetailsResponse> {
        val cache = cacheManager.getCache("userAccounts")
        return cache?.get(userId, List::class.java)?.let {
            it as? List<AccountDetailsResponse> ?: emptyList()
        } ?: loadAccountsByUserIdFromDbAndCache(userId)
    }

    private fun loadAccountsByUserIdFromDbAndCache(userId: String): List<AccountDetailsResponse> {
        val accounts = accountRepository.findByUserId(userId)
        val accountDetails = accounts.map { account ->
            AccountDetailsResponse(
                id = account.id!!,
                name = account.accountType,
                balance = account.balance,
                accountType = account.accountType,
                userId = account.userId
            )
        }
        cacheManager.getCache("userAccounts")?.put(userId, accountDetails)
        return accountDetails
    }

    fun getMonthlyExpenses(userId: String): ExpensesSummaryResponse {
        val cache = cacheManager.getCache("monthlyExpenses")
        return cache?.get(userId, ExpensesSummaryResponse::class.java)
            ?: loadMonthlyExpensesFromDbAndCache(userId)
    }

    private fun loadMonthlyExpensesFromDbAndCache(userId: String): ExpensesSummaryResponse {
        val accounts = accountRepository.findByUserId(userId)

        val transactions = mutableListOf<Transaction>()
        for (account in accounts) {
            transactions.addAll(transactionRepository.findByFromAccountId(account.id!!))
            transactions.addAll(transactionRepository.findByToAccountId(account.id!!))
        }

        val totalExpenses = transactions.sumOf { it.amount }

        val categories = transactions.groupBy { it.categoryId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val summary = ExpensesSummaryResponse(totalExpenses, categories)

        cacheManager.getCache("monthlyExpenses")?.put(userId, summary)

        return summary
    }

    fun getSubscriptionById(id: String): Subscription {
        val cache = cacheManager.getCache("subscriptions")
        return cache?.get(id, Subscription::class.java)
            ?: loadSubscriptionFromDbAndCache(id)
    }

    private fun loadSubscriptionFromDbAndCache(id: String): Subscription {
        val subscription = subscriptionRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("Subscription not found for ID: $id")
        }
        cacheManager.getCache("subscriptions")?.put(id, subscription)
        return subscription
    }

    fun getSubscriptionsByUserId(userId: String): List<Subscription> {
        val cache = cacheManager.getCache("userSubscriptions")
        return cache?.get(userId, List::class.java)?.let {
            it as? List<Subscription> ?: emptyList()
        } ?: loadSubscriptionsByUserIdFromDbAndCache(userId)
    }

    private fun loadSubscriptionsByUserIdFromDbAndCache(userId: String): List<Subscription> {
        val subscriptions = subscriptionRepository.findByUserId(userId)
        cacheManager.getCache("userSubscriptions")?.put(userId, subscriptions)
        return subscriptions
    }

    fun getUserByUsername(username: String): User {
        val cache = cacheManager.getCache("userCache")
        return cache?.get(username, User::class.java)
            ?: loadUserFromDbAndCache(username)
    }

    private fun loadUserFromDbAndCache(username: String): User {
        val user = userRepository.findByUsername(username) ?: throw UserNotFoundException("User not found with username: $username")
        cacheManager.getCache("userCache")?.put(username, user)
        return user
    }

    fun getTransactionHistory(userId: String): TransactionHistoryDetailsResponse {
        val cache = cacheManager.getCache("transactionHistory")
        return cache?.get(userId, TransactionHistoryDetailsResponse::class.java)
            ?: loadTransactionHistoryFromDbAndCache(userId)
    }

    private fun loadTransactionHistoryFromDbAndCache(userId: String): TransactionHistoryDetailsResponse {
        val transactions = transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)
        val history = transactions.map {
            it.id!! to it.amount
        }

        val response = TransactionHistoryDetailsResponse(history.joinToString("\n") { "Transaction ID: ${it.first}, Amount: ${it.second}" })

        cacheManager.getCache("transactionHistory")?.put(userId, response)

        return response
    }

    fun getMarketTrends(userId: String): MarketTrendsDetailsResponse {
        val cache = cacheManager.getCache("marketTrends")
        return cache?.get(userId, MarketTrendsDetailsResponse::class.java)
            ?: loadMarketTrendsFromDbAndCache(userId)
    }

    private fun loadMarketTrendsFromDbAndCache(userId: String): MarketTrendsDetailsResponse {
        val trends = "Market trends data here"
        val response = MarketTrendsDetailsResponse(
            trend = "Upward",
            price = "5000 SEK",
            volume = "3000 units",
            changePercent = "5%"
        )

        cacheManager.getCache("marketTrends")?.put(userId, response)

        return response
    }

    fun getScheduledPayments(userId: String): ScheduledPaymentDetailsResponse {
        val cache = cacheManager.getCache("scheduledPayments")
        return cache?.get(userId, ScheduledPaymentDetailsResponse::class.java)
            ?: loadScheduledPaymentsFromDbAndCache(userId)
    }

    private fun loadScheduledPaymentsFromDbAndCache(userId: String): ScheduledPaymentDetailsResponse {
        val payments = scheduledPaymentRepository.findByUserId(userId)
        val response = ScheduledPaymentDetailsResponse(payments.joinToString("\n") { "Scheduled Payment ID: ${it.id}, Amount: ${it.amount}" })

        cacheManager.getCache("scheduledPayments")?.put(userId, response)

        return response
    }

    fun getAccountInfo(userId: String): String {
        val cache = cacheManager.getCache("accountInfo")
        return cache?.get(userId, String::class.java)
            ?: loadAccountInfoFromDbAndCache(userId)
    }

    private fun loadAccountInfoFromDbAndCache(userId: String): String {
        val accounts = accountRepository.findByUserId(userId)
        val info = "User has ${accounts.size} accounts"
        cacheManager.getCache("accountInfo")?.put(userId, info)
        return info
    }

    fun getFinancialNews(): List<FinancialNewsDetailsResponse> {
        val cache = cacheManager.getCache("financialNews")
        return cache?.get("allNews", List::class.java) as? List<FinancialNewsDetailsResponse> ?: emptyList()
    }

    fun storeFinancialNews(news: List<FinancialNewsDetailsResponse>) {
        val cache = cacheManager.getCache("financialNews")
        cache?.put("allNews", news)
    }

    fun storeMonthlyExpenses(userId: String, expensesSummary: ExpensesSummaryResponse) {
        val cache = cacheManager.getCache("monthlyExpenses")
        cache?.put(userId, expensesSummary)
    }

    fun evictCache(cacheName: String, key: String) {
        val cache = cacheManager.getCache(cacheName)
        cache?.evict(key)
    }
}