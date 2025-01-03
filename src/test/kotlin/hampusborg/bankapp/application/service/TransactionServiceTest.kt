package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class TransactionServiceTest {

    private val repository: TransactionRepository = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val service = TransactionService(repository, cacheHelperService)

    @Test
    fun `should fetch transactions by user`() {
        val transaction = Transaction(
            id = "1",
            userId = "testUser",
            amount = 100.0,
            categoryId = TransactionCategory.TRANSFER,
            fromAccountId = "123",
            toAccountId = "456",
            date = LocalDateTime.now(),
            timestamp = System.currentTimeMillis()
        )

        whenever(repository.findByUserId("testUser")).thenReturn(listOf(transaction))

        val result = service.getTransactionsByUser("testUser")

        assertEquals(1, result.size)
        assertEquals(100.0, result[0].amount)
        assertEquals(TransactionCategory.TRANSFER, result[0].categoryId)
        verify(repository, times(1)).findByUserId("testUser")
    }

    @Test
    fun `should fetch transactions by account ID`() {
        val transaction = Transaction(
            id = "1",
            userId = "testUser",
            amount = 100.0,
            categoryId = TransactionCategory.TRANSFER,
            fromAccountId = "123",
            toAccountId = "456",
            date = LocalDateTime.now(),
            timestamp = System.currentTimeMillis()
        )

        whenever(repository.findByFromAccountIdAndUserId("123", "testUser")).thenReturn(listOf(transaction))
        whenever(repository.findByToAccountIdAndUserId("123", "testUser")).thenReturn(emptyList())

        val result = service.getTransactionsByAccountId("testUser", "123")

        assertEquals(1, result.size)
        assertEquals(100.0, result[0].amount)
        assertEquals("123", result[0].fromAccountId)
        verify(repository, times(1)).findByFromAccountIdAndUserId("123", "testUser")
    }
}
