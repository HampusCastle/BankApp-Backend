package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.exception.classes.NoTransactionsFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class BudgetService(
    private val transactionRepository: TransactionRepository,
    private val cacheHelperService: CacheHelperService
) {

    fun getMonthlyExpenses(userId: String, accountId: String): ExpensesSummaryResponse {
        val cachedExpenses = cacheHelperService.getMonthlyExpenses(userId)
        if (cachedExpenses != null) {
            return cachedExpenses
        }

        val transactions = loadTransactionsByAccountIdAndUserId(userId, accountId)

        if (transactions.isEmpty()) {
            throw NoTransactionsFoundException("No transactions found for user ID: $userId and account ID: $accountId")
        }

        val expensesSummary = calculateExpensesSummary(transactions)

        cacheHelperService.cacheMonthlyExpenses(userId, expensesSummary)
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