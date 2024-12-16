package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressSummaryResponse
import hampusborg.bankapp.application.exception.classes.NoTransactionsFoundException
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class BudgetService(
    private val transactionRepository: TransactionRepository,
    private val cacheHelperService: CacheHelperService,
    private val savingsGoalRepository: SavingsGoalRepository
) {

    fun getMonthlyExpenses(userId: String): ExpensesSummaryResponse {


        val cachedExpenses = cacheHelperService.getMonthlyExpenses(userId)
        if (cachedExpenses != null) {
            return cachedExpenses
        }

        return loadMonthlyExpensesAndCache(userId)
    }

    private fun loadMonthlyExpensesAndCache(userId: String): ExpensesSummaryResponse {
        val transactions = transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)

        if (transactions.isEmpty()) {
            throw NoTransactionsFoundException("No transactions found for user ID: $userId")
        }

        val totalExpenses = transactions.sumOf { it.amount }
        val expensesSummary = ExpensesSummaryResponse(totalExpenses)

        cacheHelperService.cacheMonthlyExpenses(userId, expensesSummary)

        return expensesSummary
    }

    fun getSavingsProgress(userId: String, savingsGoalId: String): SavingsProgressSummaryResponse {


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