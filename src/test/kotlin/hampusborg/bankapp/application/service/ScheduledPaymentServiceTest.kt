package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.ScheduledPaymentRequest
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScheduledPaymentServiceTest {

    private val scheduledPaymentRepository: ScheduledPaymentRepository = mock()
    private val scheduledPaymentService = ScheduledPaymentService(scheduledPaymentRepository)

    @Test
    fun `should create scheduled payment successfully`() {
        val request = ScheduledPaymentRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 100.0,
            schedule = "monthly",
            nextPaymentDate = System.currentTimeMillis() + 2592000000
        )
        val userId = "user123"

        val scheduledPayment = ScheduledPayment(
            userId = userId,
            amount = request.amount,
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            schedule = request.schedule,
            nextPaymentDate = request.nextPaymentDate
        )

        whenever(scheduledPaymentRepository.save(any<ScheduledPayment>())).thenReturn(scheduledPayment)

        val result = scheduledPaymentService.createScheduledPayment(request, userId)

        assertEquals(userId, result.userId)
        assertEquals(request.amount, result.amount)
        verify(scheduledPaymentRepository).save(any())
    }

    @Test
    fun `should update scheduled payment successfully`() {
        val existingPayment = ScheduledPayment(
            id = "payment1",
            userId = "user123",
            amount = 100.0,
            fromAccountId = "account1",
            toAccountId = "account2",
            schedule = "monthly",
            nextPaymentDate = System.currentTimeMillis() + 2592000000
        )

        val updatedRequest = ScheduledPaymentRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 150.0,
            schedule = "weekly",
            nextPaymentDate = System.currentTimeMillis() + 604800000
        )

        whenever(scheduledPaymentRepository.findById("payment1")).thenReturn(java.util.Optional.of(existingPayment))
        whenever(scheduledPaymentRepository.save(any<ScheduledPayment>())).thenReturn(existingPayment)

        val result = scheduledPaymentService.updateScheduledPayment("payment1", updatedRequest)

        assertEquals(updatedRequest.amount, result.amount)
        assertEquals(updatedRequest.schedule, result.schedule)
        verify(scheduledPaymentRepository).save(any())
    }

    @Test
    fun `should throw exception when updating non-existing scheduled payment`() {
        val updatedRequest = ScheduledPaymentRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 150.0,
            schedule = "weekly",
            nextPaymentDate = System.currentTimeMillis() + 604800000
        )

        whenever(scheduledPaymentRepository.findById("payment1")).thenReturn(java.util.Optional.empty())

        assertFailsWith<Exception> {
            scheduledPaymentService.updateScheduledPayment("payment1", updatedRequest)
        }
    }

    @Test
    fun `should delete scheduled payment successfully`() {
        val paymentId = "payment1"
        val existingPayment = ScheduledPayment(
            id = paymentId,
            userId = "user123",
            amount = 100.0,
            fromAccountId = "account1",
            toAccountId = "account2",
            schedule = "monthly",
            nextPaymentDate = System.currentTimeMillis() + 2592000000
        )

        whenever(scheduledPaymentRepository.findById(paymentId)).thenReturn(java.util.Optional.of(existingPayment))

        scheduledPaymentService.deleteScheduledPayment(paymentId)

        verify(scheduledPaymentRepository).delete(existingPayment)
    }

    @Test
    fun `should throw exception when deleting non-existing scheduled payment`() {
        val paymentId = "payment1"
        whenever(scheduledPaymentRepository.findById(paymentId)).thenReturn(java.util.Optional.empty())

        assertFailsWith<Exception> {
            scheduledPaymentService.deleteScheduledPayment(paymentId)
        }
    }

    @Test
    fun `should get payments due`() {
        val currentTime = System.currentTimeMillis()
        val payment1 = ScheduledPayment(
            userId = "user123",
            amount = 100.0,
            fromAccountId = "account1",
            toAccountId = "account2",
            schedule = "monthly",
            nextPaymentDate = currentTime - 1000
        )
        val payment2 = ScheduledPayment(
            userId = "user456",
            amount = 200.0,
            fromAccountId = "account3",
            toAccountId = "account4",
            schedule = "weekly",
            nextPaymentDate = currentTime + 1000
        )

        whenever(scheduledPaymentRepository.findAll()).thenReturn(listOf(payment1, payment2))

        val duePayments = scheduledPaymentService.getPaymentsDue(currentTime)

        assertEquals(1, duePayments.size)
        assertEquals(payment1, duePayments[0])
    }
}