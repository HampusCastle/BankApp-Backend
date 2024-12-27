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
) {
    private val log = LoggerFactory.getLogger(CacheHelperService::class.java)

    private fun <T> getCache(cacheName: String, key: String, clazz: Class<T>): T? {
        val cache = cacheManager.getCache(cacheName)
        return cache?.get(key, clazz)
    }

    private fun <T> putCache(cacheName: String, key: String, value: T) {
        val cache = cacheManager.getCache(cacheName)
        log.debug("CacheManager: Putting value {} with key {} into cache {}", value, key, cacheName)
        cache?.put(key, value)
        log.debug("Cache updated: {} with key: {}, value: {}", cacheName, key, value)
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

    fun evictAndStoreAllAccountsForUser(userId: String) {
        evictCache("userAccounts", userId)
        val accounts = accountRepository.findByUserId(userId)
        storeAccountsByUserId(userId, accounts)
    }

    fun evictCache(cacheName: String, key: String) {
        cacheManager.getCache(cacheName)?.evict(key)
    }
}