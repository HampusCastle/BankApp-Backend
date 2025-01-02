package hampusborg.bankapp.application.service.base

import hampusborg.bankapp.application.dto.response.*
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
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    private val log = LoggerFactory.getLogger(CacheHelperService::class.java)

    fun <T> getCache(cacheName: String, key: String, clazz: Class<T>): T? {
        val cache = cacheManager.getCache(cacheName)
        if (cache == null) {
            log.warn("Cache {} does not exist.", cacheName)
            return null
        }
        val value = cache.get(key, clazz)
        log.debug("Retrieved value from cache {}: Key: {}, Value: {}", cacheName, key, value)
        return value
    }

    private fun <T> putCache(cacheName: String, key: String, value: T) {
        val cache = cacheManager.getCache(cacheName)
        log.debug("CacheManager: Putting value {} with key {} into cache {}", value, key, cacheName)
        cache?.put(key, value)
        log.debug("Cache updated: {} with key: {}, value: {}", cacheName, key, value)
    }

    fun handleAccountCacheUpdate(userId: String) {
        log.info("Handling cache update for user: $userId")
        evictCache("userAccounts", userId)
        val accounts = accountRepository.findByUserId(userId)
        storeAccountsByUserId(userId, accounts)
    }

    fun storeScheduledPayment(paymentId: String, payment: ScheduledPayment) {
        putCache("scheduledPaymentDetails", paymentId, payment)
    }

    fun storeScheduledPaymentsForUser(userId: String, payments: List<ScheduledPayment>) {
        putCache("scheduledPayments", userId, payments)
    }

    fun storeSubscription(subscriptionId: String, subscription: Subscription) {
        putCache("subscriptionDetails", subscriptionId, subscription)
    }

    fun storeSubscriptionsByUserAndStatus(userId: String, status: String, subscriptions: List<Subscription>) {
        putCache("subscriptions", "$userId:$status", subscriptions)
    }

    fun getSubscriptionsByUserAndStatus(userId: String, status: String): List<Subscription>? {
        return getCache("subscriptions", "$userId:$status", List::class.java)
            ?.filterIsInstance<Subscription>()
    }

    fun storeTransactionsByUserId(userId: String, transactions: List<Transaction>) {
        putCache("transactionsByUser", userId, transactions)
    }

    fun getTransactionsByUserId(userId: String): List<Transaction>? {
        return getCache("transactionsByUser", userId, List::class.java)?.filterIsInstance<Transaction>()
    }

    fun storeTransactionsByAccountId(accountId: String, transactions: List<Transaction>) {
        putCache("transactionsByAccount", accountId, transactions)
    }

    fun getTransactionsByAccountId(accountId: String): List<Transaction>? {
        return getCache("transactionsByAccount", accountId, List::class.java)?.filterIsInstance<Transaction>()
    }

    fun evictTransactionsCache(userId: String) {
        evictCache("transactionsByUser", userId)
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
        log.debug("Storing accounts in cache for user $userId")
        val accountDetails = accounts.map { AccountUtils.mapToAccountDetailsResponse(it) }
        putCache("userAccounts", userId, accountDetails)
        log.debug("Cache updated for user {} with accounts: {}", userId, accountDetails)
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

    fun storeRecurringPayment(paymentId: String, payment: RecurringPayment) {
        putCache("recurringPaymentDetails", paymentId, payment)
    }

    fun storeRecurringPaymentsForUser(userId: String, payments: List<RecurringPayment>) {
        log.info("Storing recurring payments in cache for user {}: {}", userId, payments)
        putCache("recurringPayments", userId, payments)
    }

    fun storeSavingsGoalsForUser(userId: String, savingsGoals: List<SavingsGoal>) {
        putCache("savingsGoals", userId, savingsGoals)
    }

    fun evictSavingsGoalsForUser(userId: String) {
        evictCache("savingsGoals", userId)
    }

    fun refreshSubscriptionsCache(userId: String, status: String) {
        val subscriptions = subscriptionRepository.findAllByUserIdAndStatus(userId, status)
        storeSubscriptionsByUserAndStatus(userId, status, subscriptions)
    }

    fun evictSubscriptionsCache(userId: String, status: String) {
        evictCache("subscriptions", "$userId:$status")
    }

    fun evictScheduledPaymentsCache(userId: String) {
        evictCache("scheduledPayments", userId)
    }

    fun evictMonthlyExpensesCache(userId: String) {
        evictCache("monthlyExpenses", "user:$userId")
    }

    fun evictAndStoreAllAccountsForUser(userId: String) {
        evictCache("userAccounts", userId)
        val accounts = accountRepository.findByUserId(userId)
        storeAccountsByUserId(userId, accounts)
    }

    fun evictCache(cacheName: String, key: String) {
        println("Evicting cache - Name: $cacheName, Key: $key")
        cacheManager.getCache(cacheName)?.evict(key)
    }
}