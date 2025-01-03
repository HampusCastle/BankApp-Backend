package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime

class TransferServiceTest {

    private val paymentService: PaymentService = mock()
    private val notificationService: NotificationService = mock()
    private val activityLogService: ActivityLogService = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val service = TransferService(paymentService, notificationService, activityLogService, cacheHelperService)

    @Test
    fun `should transfer funds successfully`() {
        val request = InitiateTransferRequest(
            fromAccountId = "123",
            toAccountId = "456",
            amount = 50.0,
            categoryId = TransactionCategory.TRANSFER.name
        )
        val transaction = Transaction(
            id = "1",
            userId = "testUser",
            amount = 50.0,
            categoryId = TransactionCategory.TRANSFER,
            fromAccountId = "123",
            toAccountId = "456",
            date = LocalDateTime.now(),
            timestamp = System.currentTimeMillis()
        )

        whenever(paymentService.handleTransfer(request, "testUser")).thenReturn(transaction)

        val result = service.transferFunds(request, "testUser")

        assertEquals("1", result.transactionId)
        assertEquals("Transfer successful", result.message)
        verify(paymentService, times(1)).handleTransfer(request, "testUser")
        verify(notificationService, times(1)).createNotification(any())
        verify(activityLogService, times(1)).logActivity(eq("testUser"), any(), any())
        verify(cacheHelperService, times(1)).evictTransactionsCache("testUser")
    }
}