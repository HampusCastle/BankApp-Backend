package hampusborg.bankapp.application.service.base

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import hampusborg.bankapp.application.exception.classes.InsufficientFundsException
import hampusborg.bankapp.application.exception.classes.InvalidAccountException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class PaymentService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {

    @Transactional
    fun handleTransfer(request: InitiateTransferRequest, userId: String): Transaction {
        val fromAccount = getAccount(request.fromAccountId, userId)
        val toAccount = getAccount(request.toAccountId, userId)

        validateAccounts(fromAccount, toAccount, request.amount)

        fromAccount.balance -= request.amount
        toAccount.balance += request.amount
        accountRepository.saveAll(listOf(fromAccount, toAccount))

        return logTransaction(
            fromAccountId = fromAccount.id!!,
            toAccountId = toAccount.id!!,
            userId = userId,
            amount = request.amount,
            category = TransactionCategory.TRANSFER
        )
    }

    fun handleSubscriptionPayment(request: InitiateTransferRequest, userId: String): Transaction {
        return logTransaction(
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            userId = userId,
            amount = request.amount,
            category = TransactionCategory.SUBSCRIPTIONS
        )
    }

    private fun getAccount(accountId: String, userId: String): Account {
        val account = accountRepository.findById(accountId).orElseThrow {
            InvalidAccountException("Account with ID $accountId not found.")
        }
        if (account.userId != userId) {
            throw InvalidAccountException("Account does not belong to the user.")
        }
        return account
    }

    private fun validateAccounts(fromAccount: Account, toAccount: Account, amount: Double) {
        if (fromAccount.balance < amount) {
            throw InsufficientFundsException("Insufficient balance in account ${fromAccount.id}.")
        }
    }

    fun logTransaction(
        fromAccountId: String,
        toAccountId: String,
        userId: String,
        amount: Double,
        category: TransactionCategory = TransactionCategory.OTHER
    ): Transaction {
        val transaction = Transaction(
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            userId = userId,
            amount = amount,
            date = LocalDate.now().toString(),
            timestamp = System.currentTimeMillis(),
            categoryId = category
        )
        return transactionRepository.save(transaction)
    }
}