package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.application.dto.response.TransferStatusResponse
import hampusborg.bankapp.application.service.base.PaymentService
import org.springframework.stereotype.Service

@Service
class TransferService(
    private val paymentService: PaymentService,
    private val notificationService: NotificationService,
    private val activityLogService: ActivityLogService,
) {
    fun transferFunds(initiateTransferRequest: InitiateTransferRequest, userId: String): TransferStatusResponse {
        val transaction = paymentService.handleTransfer(initiateTransferRequest, userId)

        notificationService.createNotification(
            SendNotificationRequest(
                userId = userId,
                message = "Successfully transferred ${transaction.amount} from ${transaction.fromAccountId} to ${transaction.toAccountId}.",
                type = "TRANSFER"
            )
        )

        activityLogService.logActivity(
            userId,
            "Funds transferred",
            "From: ${transaction.fromAccountId}, To: ${transaction.toAccountId}, Amount: ${transaction.amount}"
        )

        return TransferStatusResponse(
            message = "Transfer successful",
            status = "success",
            transactionId = transaction.id
        )
    }
}