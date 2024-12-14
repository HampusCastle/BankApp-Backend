package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val paymentService: PaymentService,
    private val rateLimiterService: RateLimiterService
) {

    fun getTransactionHistory(userId: String): List<Transaction> {
        return transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)
    }

    fun performTransfer(initiateTransferRequest: InitiateTransferRequest, userId: String) {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }
        paymentService.handleTransfer(initiateTransferRequest, userId)
    }

    fun getFilteredTransactions(
        userId: String,
        fromDate: LocalDate,
        toDate: LocalDate,
        minAmount: Double,
        maxAmount: Double
    ): List<Transaction> {
        val transactions = transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)

        return transactions.filter {
            val transactionDate = LocalDate.parse(it.date)
            it.amount in minAmount..maxAmount && !transactionDate.isBefore(fromDate) && !transactionDate.isAfter(toDate)
        }
    }
}