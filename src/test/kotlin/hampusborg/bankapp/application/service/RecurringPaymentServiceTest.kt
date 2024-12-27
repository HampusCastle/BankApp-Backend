import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.service.RecurringPaymentService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.RecurringPayment
import hampusborg.bankapp.core.repository.RecurringPaymentRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class RecurringPaymentServiceTest {

    private val repository: RecurringPaymentRepository = mock(RecurringPaymentRepository::class.java)
    private val paymentService: RecurringPaymentService = RecurringPaymentService(
        recurringPaymentRepository = repository,
        paymentService = mock(PaymentService::class.java)
    )

    @Test
    fun `should create recurring payment`() {
        `when`(repository.save(any())).thenReturn(mock(RecurringPayment::class.java))
        paymentService.createRecurringPayment(mock(RecurringPaymentRequest::class.java))
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should fetch all recurring payments`() {
        `when`(repository.findByUserId("testUser")).thenReturn(emptyList())
        val payments = paymentService.getAllRecurringPayments("testUser")
        assertTrue(payments.isEmpty())
    }
}