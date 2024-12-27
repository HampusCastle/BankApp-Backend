import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.service.ScheduledPaymentService
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class ScheduledPaymentServiceTest {

    private val repository: ScheduledPaymentRepository = mock(ScheduledPaymentRepository::class.java)
    private val service: ScheduledPaymentService = ScheduledPaymentService(repository)

    @Test
    fun `should create scheduled payment`() {
        `when`(repository.save(any())).thenReturn(mock(ScheduledPayment::class.java))
        service.createScheduledPayment(mock(CreateScheduledPaymentRequest::class.java), "testUser")
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should fetch scheduled payments by user`() {
        `when`(repository.findByUserId("testUser")).thenReturn(emptyList())
        val payments = service.getScheduledPaymentsByUser("testUser")
        assertTrue(payments.isEmpty())
    }
}