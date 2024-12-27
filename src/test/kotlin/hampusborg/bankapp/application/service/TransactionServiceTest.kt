import hampusborg.bankapp.application.service.TransactionService
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class TransactionServiceTest {

    private val repository: TransactionRepository = mock(TransactionRepository::class.java)
    private val service: TransactionService = TransactionService(repository)

    @Test
    fun `should fetch transactions by user`() {
        `when`(repository.findByUserId("testUser")).thenReturn(emptyList())
        val transactions = service.getTransactionsByUser("testUser")
        assertTrue(transactions.isEmpty())
    }

    @Test
    fun `should fetch transactions by account ID`() {
        `when`(repository.findByFromAccountIdAndUserId("accountId", "testUser")).thenReturn(emptyList())
        val transactions = service.getTransactionsByAccountId("testUser", "accountId")
        assertTrue(transactions.isEmpty())
    }
}