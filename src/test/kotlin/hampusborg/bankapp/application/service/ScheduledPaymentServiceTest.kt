

package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ScheduledPaymentServiceTest {

    private val repository: ScheduledPaymentRepository = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val service = ScheduledPaymentService(repository, cacheHelperService)

    @Test
    fun `should create scheduled payment successfully`() {
        val request = CreateScheduledPaymentRequest(
            amount = 200.0,
            fromAccountId = "123",
            toAccountId = "456",
            schedule = "weekly",
            nextPaymentDate = System.currentTimeMillis()
        )
        val savedPayment = ScheduledPayment(
            id = "1",
            userId = "testUser",
            amount = 200.0,
            fromAccountId = "123",
            toAccountId = "456",
            schedule = "weekly",
            nextPaymentDate = System.currentTimeMillis()
        )

        whenever(repository.save(any())).thenReturn(savedPayment)

        val result = service.createScheduledPayment(request, "testUser")

        assertEquals("1", result.paymentId)
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should fetch scheduled payments by user ID`() {
        val savedPayment = ScheduledPayment(
            id = "1",
            userId = "testUser",
            amount = 200.0,
            fromAccountId = "123",
            toAccountId = "456",
            schedule = "weekly",
            nextPaymentDate = System.currentTimeMillis()
        )

        whenever(repository.findByUserId("testUser")).thenReturn(listOf(savedPayment))

        val result = service.getScheduledPaymentsByUserId("testUser")

        assertEquals(1, result.size)
        assertEquals(200.0, result[0].amount)
        verify(repository, times(1)).findByUserId("testUser")
    }
}