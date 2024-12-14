package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressSummaryResponse
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class BudgetService(
    private val transactionRepository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val rateLimiterService: RateLimiterService,
    private val cacheHelperService: CacheHelperService
) {
    fun getMonthlyExpenses(userId: String): ExpensesSummaryResponse {
        if (!rateLimiterService.isAllowed(userId)) {
            throw RuntimeException("Too many requests, please try again later.")
        }

        val expenses = cacheHelperService.getMonthlyExpenses(userId)

        if (expenses != null) {
            return expenses
        }

        val transactions = transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)

        if (transactions.isEmpty()) {
            throw Exception("No transactions found for user ID: $userId")
        }

        val totalExpenses = transactions.sumOf { it.amount }
        val categories = transactions.groupBy { it.categoryId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        val expensesResponse = ExpensesSummaryResponse(totalExpenses, categories)

        cacheHelperService.storeMonthlyExpenses(userId, expensesResponse)

        return expensesResponse
    }

    fun getSavingsProgress(userId: String, savingsGoalId: String): SavingsProgressSummaryResponse {
        if (!rateLimiterService.isAllowed(userId)) {
            throw RuntimeException("Too many requests, please try again later.")
        }

        val savingsGoal = savingsGoalRepository.findById(savingsGoalId).orElseThrow {
            UserNotFoundException("Savings goal not found for user: $userId")
        }

        val progressPercentage = (savingsGoal.currentAmount / savingsGoal.targetAmount) * 100

        return SavingsProgressSummaryResponse(
            totalSaved = savingsGoal.currentAmount,
            savingsGoal = savingsGoal.targetAmount,
            progressPercentage = progressPercentage
        )
    }
}
