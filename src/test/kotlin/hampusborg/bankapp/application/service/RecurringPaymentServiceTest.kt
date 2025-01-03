package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.RecurringPayment
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.RecurringPaymentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RecurringPaymentServiceTest {

    private val repository: RecurringPaymentRepository = mock()
    private val paymentService: PaymentService = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val service = RecurringPaymentService(repository, paymentService, cacheHelperService)

    @Test
    fun `should create recurring payment successfully`() {
        val request = RecurringPaymentRequest(
            amount = 100.0,
            fromAccountId = "123",
            toAccountId = "456",
            interval = "monthly"
        )
        val savedPayment = RecurringPayment(
            id = "1",
            userId = "testUser",
            amount = 100.0,
            fromAccountId = "123",
            toAccountId = "456",
            interval = "monthly",
            nextPaymentDate = System.currentTimeMillis(),
            categoryId = TransactionCategory.RECURRING_PAYMENT,
            status = "active"
        )

        whenever(repository.save(any())).thenReturn(savedPayment)

        val result = service.createRecurringPayment(request)

        assertEquals("1", result.id)
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should throw exception when user is not authenticated`() {
        assertThrows<ApiRequestException> {
            service.createRecurringPayment(mock())
        }
    }
}
