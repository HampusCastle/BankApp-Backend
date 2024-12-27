import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.service.ActivityLogService
import hampusborg.bankapp.application.service.NotificationService
import hampusborg.bankapp.application.service.TransferService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Transaction
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class TransferServiceTest {

    private val paymentService: PaymentService = mock(PaymentService::class.java)
    private val transferService: TransferService = TransferService(
        paymentService = paymentService,
        notificationService = mock(NotificationService::class.java),
        activityLogService = mock(ActivityLogService::class.java)
    )

    @Test
    fun `should transfer funds`() {
        `when`(paymentService.handleTransfer(any(), anyString())).thenReturn(mock(Transaction::class.java))
        transferService.transferFunds(mock(InitiateTransferRequest::class.java), "testUser")
        verify(paymentService, times(1)).handleTransfer(any(), anyString())
    }

    @Test
    fun `should log activity during transfer`() {
        transferService.transferFunds(mock(InitiateTransferRequest::class.java), "testUser")
        verify(mock(ActivityLogService::class.java), times(1)).logActivity(anyString(), anyString(), anyString())
    }
}