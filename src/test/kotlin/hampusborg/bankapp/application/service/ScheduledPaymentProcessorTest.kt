package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.response.TransferStatusResponse
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.domain.TransactionCategory
import hampusborg.bankapp.core.repository.TransactionCategoryRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class ScheduledPaymentProcessorTest {

    private val scheduledPaymentService: ScheduledPaymentService = mock()
    private val transferService: TransferService = mock()
    private val activityLogService: ActivityLogService = mock()
    private val transactionCategoryRepository: TransactionCategoryRepository = mock()

    private val scheduledPaymentProcessor = ScheduledPaymentProcessor(
        scheduledPaymentService, transferService, activityLogService, transactionCategoryRepository
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
            categoryId = null
        )

        val defaultCategory = TransactionCategory(id = "default-category-id", name = "Default")
        whenever(transactionCategoryRepository.findByName("Default")).thenReturn(defaultCategory)

        whenever(scheduledPaymentService.getPaymentsDue(now)).thenReturn(listOf(scheduledPayment))

        whenever(
            transferService.transferFunds(
                any(),
                any()
            )
        ).thenReturn(TransferStatusResponse(message = "Transfer successful"))

        doNothing().`when`(scheduledPaymentService).save(any())

        scheduledPaymentProcessor.processScheduledPayments(now)

        val captor = argumentCaptor<InitiateTransferRequest>()
        verify(transferService).transferFunds(captor.capture(), eq("user123"))

        val capturedRequest = captor.firstValue
        assert(capturedRequest.fromAccountId == "account1")
        assert(capturedRequest.toAccountId == "account2")
        assert(capturedRequest.amount == 100.0)
        assert(capturedRequest.categoryId == "default-category-id")

        verify(scheduledPaymentService).save(check { savedPayment ->
            assert(savedPayment.id == "payment-id")
            assert(savedPayment.schedule == "monthly")
            assert(savedPayment.nextPaymentDate > now)
        })
    }
}