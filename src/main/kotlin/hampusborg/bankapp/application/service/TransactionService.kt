package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.TransactionRepository
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val cacheHelperService: CacheHelperService
) {
    private val logger = LoggerFactory.getLogger(TransactionService::class.java)

    fun getTransactionsByUser(userId: String): List<Transaction> {
        logger.info("Fetching transactions for userId: $userId")

        val cachedTransactions = cacheHelperService.getTransactionsByUserId(userId)
        if (!cachedTransactions.isNullOrEmpty()) {
            logger.debug("Returning cached transactions for userId: $userId")
            return cachedTransactions
        }

        val transactions = transactionRepository.findByUserId(userId)
        logger.debug("Found ${transactions.size} transactions for userId: $userId")

        cacheHelperService.storeTransactionsByUserId(userId, transactions)

        return transactions
    }

    fun getTransactionsByAccountId(userId: String, accountId: String): List<Transaction> {
        logger.info("Fetching transactions for accountId: $accountId and userId: $userId")

        val cachedTransactions = cacheHelperService.getTransactionsByAccountId(accountId)
        if (!cachedTransactions.isNullOrEmpty()) {
            logger.debug("Returning cached transactions for accountId: $accountId")
            return cachedTransactions
        }

        val transactionsFromAccount = transactionRepository.findByFromAccountIdAndUserId(accountId, userId)
        val transactionsToAccount = transactionRepository.findByToAccountIdAndUserId(accountId, userId)

        val allTransactions = transactionsFromAccount + transactionsToAccount

        cacheHelperService.storeTransactionsByAccountId(accountId, allTransactions)
        cacheHelperService.evictTransactionsCache(userId)

        return allTransactions
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