package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest
class BudgetReportServiceTest {

    private val transactionRepository: TransactionRepository = mock()
    private val savingsGoalRepository: SavingsGoalRepository = mock()
    private val budgetReportService = BudgetReportService(transactionRepository, savingsGoalRepository)

    @Test
    fun `should calculate monthly expenses correctly`() {
        val userId = "user123"
        val transactions = listOf(
            Transaction(fromAccountId = "fromAccount", toAccountId = "toAccount", amount = 100.0, timestamp = System.currentTimeMillis(), date = "2024-12-12", userId = "user123", categoryId = "cat1"),
            Transaction(fromAccountId = "fromAccount", toAccountId = "toAccount", amount = 200.0, timestamp = System.currentTimeMillis(), date = "2024-12-12", userId = "user123", categoryId = "cat1")
        )

        whenever(transactionRepository.findByFromAccountId(userId)).thenReturn(transactions)

        val response = budgetReportService.getMonthlyExpenses(userId)

        assertEquals(300.0, response.totalExpenses)
        verify(transactionRepository).findByFromAccountId(userId)
    }

    @Test
    fun `should calculate savings progress correctly`() {
        val userId = "user123"
        val savingsGoalId = "goal123"
        val savingsGoal = SavingsGoal(
            id = savingsGoalId,
            userId = userId,
            name = "Vacation",
            targetAmount = 5000.0,
            targetDate = LocalDate.now(),
            currentAmount = 1000.0
        )

        whenever(savingsGoalRepository.findById(savingsGoalId)).thenReturn(Optional.of(savingsGoal))

        val response = budgetReportService.getSavingsProgress(userId, savingsGoalId)

        assertEquals(1000.0, response.totalSaved)
        verify(savingsGoalRepository).findById(savingsGoalId)
    }

    @Test
    fun `should throw exception when savings goal is not found`() {
        val userId = "user123"
        val savingsGoalId = "goal123"

        whenever(savingsGoalRepository.findById(savingsGoalId)).thenReturn(Optional.empty())

        val exception = assertThrows<UserNotFoundException> {
            budgetReportService.getSavingsProgress(userId, savingsGoalId)
        }

        assertEquals("Savings goal not found for user: $userId", exception.message)
    }
}