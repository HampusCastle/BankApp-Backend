package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    fun getFilteredTransactions(
        userId: String,
        fromDate: LocalDate? = null,
        toDate: LocalDate? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
        categoryId: String? = null,
        fromAccountId: String? = null,
        toAccountId: String? = null
    ): List<Transaction> {
        val transactions = transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)
        return transactions.filter { transaction ->
            val transactionDate = Instant.ofEpochMilli(transaction.timestamp).atZone(ZoneOffset.UTC).toLocalDate()
            (fromDate == null || !transactionDate.isBefore(fromDate)) &&
                    (toDate == null || !transactionDate.isAfter(toDate)) &&
                    (minAmount == null || transaction.amount >= minAmount) &&
                    (maxAmount == null || transaction.amount <= maxAmount) &&
                    (categoryId == null || transaction.categoryId == categoryId) &&
                    (fromAccountId == null || transaction.fromAccountId == fromAccountId) &&
                    (toAccountId == null || transaction.toAccountId == toAccountId)
        }
    }
    fun getTransactionHistory(userId: String): List<Transaction> {
        logger.info("Fetching transaction history for user: $userId")
        val transactions = transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)
        logger.info("Found ${transactions.size} transactions.")
        return transactions
    }
}