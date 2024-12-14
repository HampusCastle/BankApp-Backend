package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.Notification
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class TransferServiceTest {

    private val paymentService: PaymentService = mock()
    private val notificationService: NotificationService = mock()
    private val activityLogService: ActivityLogService = mock()
    private val accountRepository: AccountRepository = mock()

    private val transferService = TransferService(paymentService, notificationService, activityLogService)

    @Test
    fun `should transfer funds between accounts successfully`() {
        val initiateTransferRequest = InitiateTransferRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 100.0,
            categoryId = "category1"
        )
        val userId = "user1"

        val fromAccount = Account(id = "account1", userId = "user1", accountType = "Checking", balance = 200.0)
        val toAccount = Account(id = "account2", userId = "user1", accountType = "Checking", balance = 100.0)

        whenever(accountRepository.findById(initiateTransferRequest.fromAccountId)).thenReturn(Optional.of(fromAccount))
        whenever(accountRepository.findById(initiateTransferRequest.toAccountId)).thenReturn(Optional.of(toAccount))

        val transaction = Transaction(
            fromAccountId = "account1",
            toAccountId = "account2",
            userId = "user1",
            amount = 100.0,
            timestamp = System.currentTimeMillis(),
            categoryId = "category1",
            date = "2024-12-13"
        )

        whenever(paymentService.handleTransfer(initiateTransferRequest, userId)).thenReturn(transaction)

        whenever(notificationService.createNotification(any())).thenReturn(
            Notification(
                id = "notification-id",
                userId = "user1",
                message = "Transfer Completed",
                type = "TRANSFER",
                timestamp = System.currentTimeMillis(),
            )
        )

        doNothing().`when`(activityLogService).logActivity(any(), any(), any())

        val transferResponse = transferService.transferFunds(initiateTransferRequest, userId)

        assertEquals("Transfer successful", transferResponse.message)

        verify(notificationService).createNotification(any())
        verify(activityLogService).logActivity(any(), any(), any())
        verify(paymentService).handleTransfer(initiateTransferRequest, userId)
    }
}