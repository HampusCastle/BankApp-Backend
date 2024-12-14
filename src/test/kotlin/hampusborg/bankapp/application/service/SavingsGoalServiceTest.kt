package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDate
import java.util.*
import kotlin.test.assertFailsWith

class SavingsGoalServiceTest {

    private val savingsGoalRepository: SavingsGoalRepository = mock()
    private val paymentService: PaymentService = mock()
    private val cacheHelperService: CacheHelperService = mock()  // Mock CacheHelperService
    private val rateLimiterService: RateLimiterService = mock()  // Mock RateLimiterService

    private val savingsGoalService = SavingsGoalService(
        savingsGoalRepository,
        paymentService,
        rateLimiterService,
        cacheHelperService
    )

    @Test
    fun `should create a savings goal successfully`() {
        val savingsGoal = SavingsGoal(
            name = "Emergency Fund",
            userId = "user123",
            targetAmount = 1000.0,
            targetDate = LocalDate.now().plusMonths(6),
            accountId = "123"
        )

        whenever(rateLimiterService.isAllowed("user123")).thenReturn(true)  // Mock to always allow
        whenever(savingsGoalRepository.save(any<SavingsGoal>())).thenReturn(savingsGoal)

        val result = savingsGoalService.createSavingsGoal(savingsGoal)

        assert(result.name == "Emergency Fund")
        assert(result.targetAmount == 1000.0)
        verify(savingsGoalRepository).save(any())
    }

    @Test
    fun `should throw SavingsGoalNotFoundException when goal does not exist`() {
        val goalId = "nonexistent-id"

        whenever(rateLimiterService.isAllowed(goalId)).thenReturn(true)

        whenever(cacheHelperService.getSavingsGoal(goalId)).thenReturn(null)

        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.empty())

        val exception = assertFailsWith<SavingsGoalNotFoundException> {
            savingsGoalService.getSavingsGoal(goalId)
        }

        assertEquals("Savings goal not found for ID: $goalId", exception.message)
    }

    @Test
    fun `should update an existing savings goal successfully`() {
        val goalId = "123"
        val existingGoal = SavingsGoal(
            id = goalId,
            name = "Emergency Fund",
            userId = "user123",
            targetAmount = 1000.0,
            targetDate = LocalDate.now().plusMonths(6),
            currentAmount = 200.0,
            accountId = "123"
        )
        val updatedGoal = SavingsGoal(
            id = goalId,
            name = "Emergency Fund",
            userId = "user123",
            targetAmount = 1000.0,
            targetDate = LocalDate.now().plusMonths(6),
            currentAmount = 500.0,
            accountId = "123"
        )

        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)  // Mock rate limiter
        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.of(existingGoal))
        whenever(savingsGoalRepository.save(any<SavingsGoal>())).thenReturn(existingGoal.copy(currentAmount = existingGoal.currentAmount + updatedGoal.currentAmount))

        val result = savingsGoalService.updateSavingsGoal(goalId, updatedGoal)

        assert(result.currentAmount == 700.0)
        verify(savingsGoalRepository).save(any())
    }

    @Test
    fun `should throw SavingsGoalNotFoundException when updating non-existent goal`() {
        val goalId = "nonexistent-id"
        val savingsGoal = SavingsGoal(name = "Emergency Fund", userId = "user123", targetAmount = 1000.0, accountId = "123", targetDate = LocalDate.now().plusMonths(6))

        whenever(rateLimiterService.isAllowed(goalId)).thenReturn(true)  // Mock rate limiter
        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.empty())

        assertFailsWith<SavingsGoalNotFoundException> {
            savingsGoalService.updateSavingsGoal(goalId, savingsGoal)
        }
    }

    @Test
    fun `should delete a savings goal successfully`() {
        val goalId = "123"
        val goal = SavingsGoal(
            id = goalId,
            name = "Emergency Fund",
            userId = "user123",
            targetAmount = 1000.0,
            targetDate = LocalDate.now().plusMonths(6),
            accountId = "123"
        )
        whenever(rateLimiterService.isAllowed(goalId)).thenReturn(true)  // Mock rate limiter
        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.of(goal))

        savingsGoalService.deleteSavingsGoal(goalId)

        verify(savingsGoalRepository).delete(any())
    }

    @Test
    fun `should throw SavingsGoalNotFoundException when deleting non-existent goal`() {
        val goalId = "nonexistent-id"
        whenever(rateLimiterService.isAllowed(goalId)).thenReturn(true)  // Mock rate limiter
        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.empty())

        assertFailsWith<SavingsGoalNotFoundException> {
            savingsGoalService.deleteSavingsGoal(goalId)
        }
    }
}