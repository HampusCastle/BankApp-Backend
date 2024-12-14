package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.response.TransferStatusResponse
import hampusborg.bankapp.application.exception.classes.InsufficientFundsException
import hampusborg.bankapp.application.exception.classes.InvalidAccountException
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

open class TransferService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationService: NotificationService,
    private val activityLogService: ActivityLogService
) {
    private val logger = LoggerFactory.getLogger(TransferService::class.java)

    @Transactional
    open fun transferFunds(initiateTransferRequest: InitiateTransferRequest, userId: String): TransferStatusResponse {
        logger.info("Initiating transfer for user $userId")

        val fromAccount = accountRepository.findById(initiateTransferRequest.fromAccountId)
            .orElseThrow { InvalidAccountException("From account not found.") }

        val toAccount = accountRepository.findById(initiateTransferRequest.toAccountId)
            .orElseThrow { InvalidAccountException("To account not found.") }

        if (fromAccount.userId != userId || toAccount.userId != userId) {
            throw InvalidAccountException("Transfer failed: invalid accounts.")
        }

        if (fromAccount.balance < initiateTransferRequest.amount) {
            throw InsufficientFundsException("Insufficient balance.")
        }

        fromAccount.balance -= initiateTransferRequest.amount
        toAccount.balance += initiateTransferRequest.amount

        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        val transaction = Transaction(
            fromAccountId = initiateTransferRequest.fromAccountId,
            toAccountId = initiateTransferRequest.toAccountId,
            userId = userId,
            date = "2024-12-12",
            amount = initiateTransferRequest.amount,
            timestamp = System.currentTimeMillis(),
            categoryId = initiateTransferRequest.categoryId
        )
        transactionRepository.save(transaction)

        val sendNotificationRequest = SendNotificationRequest(
            userId = userId,
            message = "Successfully transferred ${initiateTransferRequest.amount} from ${initiateTransferRequest.fromAccountId} to ${initiateTransferRequest.toAccountId}.",
            type = "TRANSFER"
        )
        notificationService.createNotification(sendNotificationRequest)

        activityLogService.logActivity(
            userId,
            "Funds transferred",
            "From: ${initiateTransferRequest.fromAccountId}, To: ${initiateTransferRequest.toAccountId}, Amount: ${initiateTransferRequest.amount}"
        )

        logger.info("Transfer completed successfully")
        return TransferStatusResponse(message = "Transfer successful")
    }
}
