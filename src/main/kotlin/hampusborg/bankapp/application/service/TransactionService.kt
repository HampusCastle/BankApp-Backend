package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.TransactionRepository
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    fun getTransactionsByUser(userId: String): List<Transaction> {
        logger.info("Fetching transactions for userId: $userId")
        val transactions = transactionRepository.findByUserId(userId)
        logger.debug("Found ${transactions.size} transactions for userId: $userId")
        return transactions
    }

    fun getTransactionsByAccountId(userId: String, accountId: String): List<Transaction> {
        logger.info("Fetching transactions for accountId: $accountId and userId: $userId")

        val transactionsFromAccount = transactionRepository.findByFromAccountIdAndUserId(accountId, userId)
        val transactionsToAccount = transactionRepository.findByToAccountIdAndUserId(accountId, userId)

        logger.debug("Found ${transactionsFromAccount.size} transactions from account.")
        logger.debug("Found ${transactionsToAccount.size} transactions to account.")

        return transactionsFromAccount + transactionsToAccount
    }

    fun getFilteredTransactions(
        userId: String,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        category: TransactionCategory?,
        minAmount: Double?,
        maxAmount: Double?,
        accountId: String
    ): List<Transaction> {
        logger.info("Filtering transactions for userId: $userId, accountId: $accountId")

        val transactions = getTransactionsByAccountId(userId, accountId)

        logger.debug("Filtering transactions based on provided criteria.")

        return transactions.filter { transaction ->
            val transactionDate = transaction.date.toLocalDate()

            val isWithinDate = (fromDate == null || !transactionDate.isBefore(fromDate)) &&
                    (toDate == null || !transactionDate.isAfter(toDate))

            val isWithinCategory = category == null || transaction.categoryId == category
            val isWithinAmount = (minAmount == null || transaction.amount >= minAmount) &&
                    (maxAmount == null || transaction.amount <= maxAmount)

            isWithinDate && isWithinCategory && isWithinAmount
        }.also {
            logger.debug("Filtered ${it.size} transactions.")
        }
    }
}
