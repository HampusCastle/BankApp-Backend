import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.service.SavingsGoalService
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class SavingsGoalServiceTest {

    private val repository: SavingsGoalRepository = mock(SavingsGoalRepository::class.java)
    private val service: SavingsGoalService = SavingsGoalService(
        savingsGoalRepository = repository,
        accountRepository = mock(AccountRepository::class.java),
        cacheHelperService = mock(CacheHelperService::class.java),
        paymentService = mock(PaymentService::class.java)
    )

    @Test
    fun `should create savings goal`() {
        `when`(repository.save(any())).thenReturn(mock(SavingsGoal::class.java))
        service.createSavingsGoal(mock(CreateSavingsGoalRequest::class.java), "testUser")
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should get savings goals by user ID`() {
        `when`(repository.findByUserId("testUser")).thenReturn(emptyList())
        val goals = service.getSavingsGoalsByUserId("testUser")
        assertTrue(goals.isEmpty())
    }
}