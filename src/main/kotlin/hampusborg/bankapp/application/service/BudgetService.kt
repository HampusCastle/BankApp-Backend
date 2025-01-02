package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.exception.classes.NoTransactionsFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BudgetService(
    private val transactionRepository: TransactionRepository,
    private val cacheHelperService: CacheHelperService,
    private val accountRepository: AccountRepository
) {
    private val logger = LoggerFactory.getLogger(BudgetService::class.java)

    fun getMonthlyExpensesForAllAccounts(userId: String): ExpensesSummaryResponse {
        logger.debug("Fetching monthly expenses for userId: $userId")

        val cachedExpenses = cacheHelperService.getMonthlyExpenses("user:$userId")
        if (cachedExpenses != null) {
            logger.debug("Returning cached expenses for userId: $userId")
            return cachedExpenses
        }

        logger.info("Cache miss for userId: $userId, recalculating expenses...")
        val accounts = accountRepository.findByUserId(userId)
        val accountTransactions = accounts.flatMap { account ->
            account.id?.let { loadTransactionsByAccountIdAndUserId(userId, it) } ?: emptyList()
        }

        val subscriptions = cacheHelperService.getSubscriptionsByUserAndStatus(userId, "ACTIVE") ?: emptyList()
        val scheduledPayments = cacheHelperService.getCache("scheduledPayments", userId, List::class.java)
            ?.filterIsInstance<ScheduledPayment>() ?: emptyList()

        val allExpenses = accountTransactions +
                subscriptions.map {
                    Transaction(
                        amount = it.amount,
                        userId = userId,
                        fromAccountId = "subscription",
                        toAccountId = "user_account",
                        categoryId = TransactionCategory.SUBSCRIPTION,
                        date = it.date ?: LocalDateTime.now(),
                        timestamp = System.currentTimeMillis()
                    )
                } +
                scheduledPayments.map {
                    Transaction(
                        amount = it.amount,
                        userId = userId,
                        fromAccountId = it.fromAccountId ?: "scheduled_payment",
                        toAccountId = it.toAccountId ?: "user_account",
                        categoryId = TransactionCategory.SCHEDULED_PAYMENT,
                        date = it.date ?: LocalDateTime.now(),
                        timestamp = System.currentTimeMillis()
                    )
                }

        if (allExpenses.isEmpty()) {
            logger.warn("No transactions found for userId: $userId")
            throw NoTransactionsFoundException("No transactions found for userId: $userId across all accounts")
        }

        val expensesSummary = calculateExpensesSummary(allExpenses)
        cacheHelperService.cacheMonthlyExpenses("user:$userId", expensesSummary)

        return expensesSummary
    }

    private fun loadTransactionsByAccountIdAndUserId(userId: String, accountId: String): List<Transaction> {
        val transactionsFromAccount = transactionRepository.findByFromAccountIdAndUserId(accountId, userId)
        val transactionsToAccount = transactionRepository.findByToAccountIdAndUserId(accountId, userId)
        return transactionsFromAccount + transactionsToAccount
    }

    private fun calculateExpensesSummary(transactions: List<Transaction>): ExpensesSummaryResponse {
        val totalExpenses = transactions.sumOf { it.amount }
        val categories = transactions
            .groupBy { it.categoryId.name }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        return ExpensesSummaryResponse(totalExpenses, categories)
    }
}