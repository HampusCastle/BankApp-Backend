package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.application.dto.response.TransferStatusResponse
import hampusborg.bankapp.application.exception.classes.InsufficientFundsException
import hampusborg.bankapp.application.exception.classes.InvalidAccountException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class TransferService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationService: NotificationService,
    private val activityLogService: ActivityLogService,
    private val cacheHelperService: CacheHelperService
) {

    @Transactional
    fun transferFunds(request: InitiateTransferRequest, userId: String): TransferStatusResponse {
        val fromAccount = accountRepository.findById(request.fromAccountId).orElseThrow {
            InvalidAccountException("Source account not found.")
        }
        val toAccount = accountRepository.findById(request.toAccountId).orElseThrow {
            InvalidAccountException("Destination account not found.")
        }

        if (fromAccount.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Source account does not belong to the user.")
        }

        if (fromAccount.balance < request.amount) {
            throw InsufficientFundsException("Insufficient funds in account ${fromAccount.id}.")
        }

        fromAccount.balance -= request.amount
        toAccount.balance += request.amount

        accountRepository.save(fromAccount)
        accountRepository.save(toAccount)

        cacheHelperService.evictCache("userAccounts", userId)
        cacheHelperService.storeAccountsByUserId(userId, listOf(fromAccount, toAccount))

        val transaction = Transaction(
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            userId = userId,
            amount = request.amount,
            timestamp = System.currentTimeMillis(),
            date = LocalDateTime.now().toString(),
            categoryId = TransactionCategory.SENT
        )

        transactionRepository.save(transaction)

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