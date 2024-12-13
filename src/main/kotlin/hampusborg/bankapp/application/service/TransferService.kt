package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.NotificationRequest
import hampusborg.bankapp.application.dto.request.TransferRequest
import hampusborg.bankapp.application.dto.response.TransferResponse
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
    private val userActivityLogService: UserActivityLogService
) {
    private val logger = LoggerFactory.getLogger(TransferService::class.java)

    @Transactional
    open fun transferFunds(transferRequest: TransferRequest, userId: String): TransferResponse {
        logger.info("Initiating transfer for user $userId")

        val fromAccount = accountRepository.findById(transferRequest.fromAccountId)
            .orElseThrow { InvalidAccountException("From account not found.") }

        val toAccount = accountRepository.findById(transferRequest.toAccountId)
            .orElseThrow { InvalidAccountException("To account not found.") }

        if (fromAccount.userId != userId || toAccount.userId != userId) {
            throw InvalidAccountException("Transfer failed: invalid accounts.")
        }

        if (fromAccount.balance < transferRequest.amount) {
            throw InsufficientFundsException("Insufficient balance.")
        }

        fromAccount.balance -= transferRequest.amount
        toAccount.balance += transferRequest.amount

        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        val transaction = Transaction(
            fromAccountId = transferRequest.fromAccountId,
            toAccountId = transferRequest.toAccountId,
            userId = userId,
            date = "2024-12-12",
            amount = transferRequest.amount,
            timestamp = System.currentTimeMillis(),
            categoryId = transferRequest.categoryId
        )
        transactionRepository.save(transaction)

        val notificationRequest = NotificationRequest(
            userId = userId,
            message = "Successfully transferred ${transferRequest.amount} from ${transferRequest.fromAccountId} to ${transferRequest.toAccountId}.",
            type = "TRANSFER"
        )
        notificationService.createNotification(notificationRequest)

        userActivityLogService.logActivity(
            userId,
            "Funds transferred",
            "From: ${transferRequest.fromAccountId}, To: ${transferRequest.toAccountId}, Amount: ${transferRequest.amount}"
        )

        logger.info("Transfer completed successfully")
        return TransferResponse(message = "Transfer successful")
    }
}
