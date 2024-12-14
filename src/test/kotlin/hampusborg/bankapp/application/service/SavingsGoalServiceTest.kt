package hampusborg.bankapp.application.service
import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.util.*

@SpringBootTest
class SavingsGoalServiceTest {

    private val savingsGoalRepository: SavingsGoalRepository = mock()
    private val paymentService: PaymentService = mock()
    private val savingsGoalService = SavingsGoalService(savingsGoalRepository, paymentService)  // Inject mocked dependencies


    @Test
    fun `should create a savings goal successfully`() {
        val savingsGoal = SavingsGoal(
            name = "Emergency Fund",
            userId = "user123",
            targetAmount = 1000.0,
            targetDate = LocalDate.now().plusMonths(6),
            accountId = "123"
        )
        whenever(savingsGoalRepository.save(any<SavingsGoal>())).thenReturn(savingsGoal)

        val result = savingsGoalService.createSavingsGoal(savingsGoal)

        assert(result.name == "Emergency Fund")
        assert(result.targetAmount == 1000.0)
        verify(savingsGoalRepository).save(any())
    }

    @Test
    fun `should throw SavingsGoalNotFoundException when goal does not exist`() {
        val goalId = "nonexistent-id"
        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.empty())

        assertThrows<SavingsGoalNotFoundException> {
            savingsGoalService.getSavingsGoal(goalId)
        }
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

        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.empty())

        assertThrows<SavingsGoalNotFoundException> {
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
        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.of(goal))

        savingsGoalService.deleteSavingsGoal(goalId)

        verify(savingsGoalRepository).delete(any())
    }

    @Test
    fun `should throw SavingsGoalNotFoundException when deleting non-existent goal`() {

        val goalId = "nonexistent-id"
        whenever(savingsGoalRepository.findById(goalId)).thenReturn(Optional.empty())

        assertThrows<SavingsGoalNotFoundException> {
            savingsGoalService.deleteSavingsGoal(goalId)
        }
    }
}