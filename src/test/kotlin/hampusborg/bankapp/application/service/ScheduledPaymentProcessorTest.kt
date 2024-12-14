package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.domain.TransactionCategory
import hampusborg.bankapp.core.repository.TransactionCategoryRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDate

class ScheduledPaymentProcessorTest {

    private val scheduledPaymentService: ScheduledPaymentService = mock()
    private val paymentService: PaymentService = mock()
    private val activityLogService: ActivityLogService = mock()
    private val transactionCategoryRepository: TransactionCategoryRepository = mock()

    private val scheduledPaymentProcessor = ScheduledPaymentProcessor(
        scheduledPaymentService, paymentService, activityLogService
    )

    @Test
    fun `should process scheduled payments successfully and invoke transferFunds`() {
        val now = 1_000_000L

        val scheduledPayment = ScheduledPayment(
            id = "payment-id",
            userId = "user123",
            amount = 100.0,
            fromAccountId = "account1",
            toAccountId = "account2",
            schedule = "monthly",
            nextPaymentDate = now - 1000,
            categoryId = "default-category-id"
        )

        val defaultCategory = TransactionCategory(id = "default-category-id", name = "Default")
        whenever(transactionCategoryRepository.findByName("Default")).thenReturn(defaultCategory)

        whenever(scheduledPaymentService.getPaymentsDue(now)).thenReturn(listOf(scheduledPayment))

        val transaction = Transaction(
            fromAccountId = "account1",
            toAccountId = "account2",
            userId = "user123",
            amount = 100.0,
            timestamp = System.currentTimeMillis(),
            categoryId = "default-category-id",
            date = LocalDate.now().toString()
        )

        whenever(paymentService.handleTransfer(any(), eq("user123"))).thenReturn(transaction)
        doNothing().`when`(scheduledPaymentService).save(any())

        scheduledPaymentProcessor.processScheduledPayments(now)

        val captor = argumentCaptor<InitiateTransferRequest>()
        verify(paymentService).handleTransfer(captor.capture(), eq("user123"))

        val capturedRequest = captor.firstValue
        println("Captured Request: ${capturedRequest.fromAccountId}, ${capturedRequest.toAccountId}, ${capturedRequest.amount}, ${capturedRequest.categoryId}")

        assert(capturedRequest.fromAccountId == "account1")
        assert(capturedRequest.toAccountId == "account2")
        assert(capturedRequest.amount == 100.0)
        assert(capturedRequest.categoryId == "default-category-id")

        verify(scheduledPaymentService).save(check { savedPayment ->
            println("Saved Payment: ${savedPayment.id}, ${savedPayment.schedule}, ${savedPayment.nextPaymentDate}")
            assert(savedPayment.id == "payment-id")
            assert(savedPayment.schedule == "monthly")
            assert(savedPayment.nextPaymentDate > now)
        })
    }
}