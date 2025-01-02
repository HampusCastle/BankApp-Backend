package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.application.dto.response.TransferStatusResponse
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransferService(
    private val paymentService: PaymentService,
    private val notificationService: NotificationService,
    private val activityLogService: ActivityLogService,
    private val cacheHelperService: CacheHelperService
) {

    @Transactional
    fun transferFunds(
        request: InitiateTransferRequest,
        userId: String,
        isSubscription: Boolean = false
    ): TransferStatusResponse {
        val transaction = paymentService.handleTransfer(request, userId)

        val category = if (isSubscription) TransactionCategory.SUBSCRIPTIONS else TransactionCategory.TRANSFER

        cacheHelperService.evictTransactionsCache(userId)

        notificationService.createNotification(
            SendNotificationRequest(
                userId = userId,
                message = "You successfully transferred ${request.amount} from ${request.fromAccountId} to ${request.toAccountId}",
                type = "TRANSFER"
            )
        )

        activityLogService.logActivity(
            userId,
            "Funds Transferred",
            "Transferred ${request.amount} from ${request.fromAccountId} to ${request.toAccountId}"
        )

        return TransferStatusResponse(
            message = "Transfer successful",
            status = "success",
            transactionId = transaction.id ?: "Unknown Transaction ID"
        )
    }
}