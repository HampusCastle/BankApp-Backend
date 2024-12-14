package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressSummaryResponse
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BudgetService(
    private val transactionRepository: TransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository
) {

    private val logger = LoggerFactory.getLogger(BudgetService::class.java)

    fun getMonthlyExpenses(userId: String): ExpensesSummaryResponse {
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
        return ExpensesSummaryResponse(totalExpenses, categories)
    }



    fun getSavingsProgress(userId: String, savingsGoalId: String): SavingsProgressSummaryResponse {
        logger.info("Fetching savings progress for user: $userId, goal ID: $savingsGoalId")
        val savingsGoal = savingsGoalRepository.findById(savingsGoalId).orElseThrow {
            throw UserNotFoundException("Savings goal not found for user: $userId")
        }
        val progressPercentage = (savingsGoal.currentAmount / savingsGoal.targetAmount) * 100
        logger.info("Current amount for savings goal $savingsGoalId: ${savingsGoal.currentAmount}")
        return SavingsProgressSummaryResponse(
            totalSaved = savingsGoal.currentAmount,
            savingsGoal = savingsGoal.targetAmount,
            progressPercentage = progressPercentage
        )
    }
}
