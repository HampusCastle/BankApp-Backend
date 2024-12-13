package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.MonthlyExpensesResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressResponse
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BudgetReportService(
    private val transactionRepository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository
) {

    private val logger = LoggerFactory.getLogger(BudgetReportService::class.java)

    fun getMonthlyExpenses(userId: String): MonthlyExpensesResponse {
        logger.info("Fetching monthly expenses for user: $userId")

        val transactions = transactionRepository.findByFromAccountId(userId)

        transactions.forEach { transaction ->
            logger.info("Transaction categoryId: ${transaction.categoryId}, Amount: ${transaction.amount}")
        }

        val totalExpenses = transactions.sumOf { it.amount }

        val categories = transactions.groupBy { it.categoryId }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        categories.forEach { (categoryId, amount) ->
            logger.info("Category ID: $categoryId, Total: $amount")
        }

        logger.info("Total monthly expenses for user $userId: $totalExpenses")
        return MonthlyExpensesResponse(totalExpenses, categories)
    }



    fun getSavingsProgress(userId: String, savingsGoalId: String): SavingsProgressResponse {
        logger.info("Fetching savings progress for user: $userId, goal ID: $savingsGoalId")
        val savingsGoal = savingsGoalRepository.findById(savingsGoalId).orElseThrow {
            throw UserNotFoundException("Savings goal not found for user: $userId")
        }
        val progressPercentage = (savingsGoal.currentAmount / savingsGoal.targetAmount) * 100
        logger.info("Current amount for savings goal $savingsGoalId: ${savingsGoal.currentAmount}")
        return SavingsProgressResponse(
            totalSaved = savingsGoal.currentAmount,
            savingsGoal = savingsGoal.targetAmount,
            progressPercentage = progressPercentage
        )
    }
}
