package hampusborg.bankapp.application.service.base

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import hampusborg.bankapp.application.exception.classes.InsufficientFundsException
import hampusborg.bankapp.application.exception.classes.InvalidAccountException
import hampusborg.bankapp.application.service.NotificationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val paymentGatewayService: PaymentGatewayService,
    private val notificationService: NotificationService
) {

    @Transactional
    fun handleTransfer(initiateTransferRequest: InitiateTransferRequest, userId: String): Transaction {
        val fromAccount = getAccount(initiateTransferRequest.fromAccountId, userId)
        val toAccount = getAccount(initiateTransferRequest.toAccountId, userId)

        validateAccounts(fromAccount, toAccount, initiateTransferRequest.amount)

        val paymentSuccess = paymentGatewayService.processPayment(
            fromAccountId = initiateTransferRequest.fromAccountId,
            toAccountId = initiateTransferRequest.toAccountId,
            amount = initiateTransferRequest.amount
        )

        if (paymentSuccess) {
            fromAccount.balance -= initiateTransferRequest.amount
            toAccount.balance += initiateTransferRequest.amount

            accountRepository.save(fromAccount)
            accountRepository.save(toAccount)

            val currentDate = java.time.LocalDate.now().toString()

            val transaction = Transaction(
                fromAccountId = initiateTransferRequest.fromAccountId,
                toAccountId = initiateTransferRequest.toAccountId,
                userId = userId,
                amount = initiateTransferRequest.amount,
                timestamp = System.currentTimeMillis(),
                categoryId = initiateTransferRequest.categoryId ?: "default-category",
                date = currentDate
            )
            transactionRepository.save(transaction)

            notificationService.createNotification(
                SendNotificationRequest(
                    userId = userId,
                    message = "Your transaction of ${initiateTransferRequest.amount} was successful.",
                    type = "Transaction"
                )
            )

            return transaction
        } else {
            throw Exception("Payment processing failed.")
        }
    }

    private fun getAccount(accountId: String, userId: String): Account {
        val account = accountRepository.findById(accountId).orElseThrow {
            throw InvalidAccountException("Account with ID $accountId not found.")
        }
        if (account.userId != userId) {
            throw InvalidAccountException("Account does not belong to the user.")
        }
        return account
    }

    private fun validateAccounts(
        fromAccount: Account,
        toAccount: Account,
        amount: Double
    ) {
        if (fromAccount.balance < amount) {
            throw InsufficientFundsException("Insufficient balance in account ${fromAccount.id}.")
        }
    }
    fun handleSubscriptionPayment(request: InitiateTransferRequest, userId: String): Transaction {
        val transaction = Transaction(
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            userId = userId,
            amount = request.amount,
            timestamp = System.currentTimeMillis(),
            categoryId = request.categoryId ?: "subscription",
            date = java.time.LocalDate.now().toString()
        )

        return transaction
    }
}