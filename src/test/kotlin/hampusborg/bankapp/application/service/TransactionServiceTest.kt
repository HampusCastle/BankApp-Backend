package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate

@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class TransactionServiceTest {

    private val transactionRepository: TransactionRepository = mock()
    private val paymentService: PaymentService = mock()
    private val rateLimiterService: RateLimiterService = mock()
    private val transactionService = TransactionService(transactionRepository, paymentService, rateLimiterService)

    @Test
    fun `should return filtered transactions`() {
        val userId = "user1"
        val fromDate = LocalDate.of(2024, 1, 1)
        val toDate = LocalDate.of(2024, 12, 31)
        val minAmount = 10.0
        val maxAmount = 1000.0

        val transaction = Transaction(
            fromAccountId = "account1", toAccountId = "account2",
            amount = 50.0, timestamp = System.currentTimeMillis(), date = "2024-12-12", userId = "userId", categoryId = "cat1"
        )

        whenever(transactionRepository.findByFromAccountId(userId)).thenReturn(listOf(transaction))
        whenever(transactionRepository.findByToAccountId(userId)).thenReturn(emptyList())
        whenever(rateLimiterService.isAllowed(userId)).thenReturn(true)

        val transactions = transactionService.getFilteredTransactions(userId, fromDate, toDate, minAmount, maxAmount)

        assert(transactions.isNotEmpty())
        assert(transactions[0].amount == 50.0)
    }

    @Test
    fun `should return all transactions for user`() {
        val userId = "user1"
        val transaction = Transaction(
            fromAccountId = "account1", toAccountId = "account2",
            amount = 50.0, timestamp = System.currentTimeMillis(), date = "2024-12-12", userId = "userId", categoryId = "cat1"
        )

        whenever(transactionRepository.findByFromAccountId(userId)).thenReturn(listOf(transaction))
        whenever(transactionRepository.findByToAccountId(userId)).thenReturn(emptyList())
        whenever(rateLimiterService.isAllowed(userId)).thenReturn(true)

        val transactions = transactionService.getTransactionHistory(userId)

        assert(transactions.isNotEmpty())
        assert(transactions[0].amount == 50.0)
    }
}