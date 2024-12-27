import hampusborg.bankapp.application.service.BudgetService
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class BudgetServiceTest {

    private val transactionRepository: TransactionRepository = mock(TransactionRepository::class.java)
    private val accountRepository: AccountRepository = mock(AccountRepository::class.java)
    private val budgetService: BudgetService = BudgetService(
        transactionRepository = transactionRepository,
        cacheHelperService = mock(CacheHelperService::class.java),
        accountRepository = accountRepository
    )

    @Test
    fun `should return empty expenses summary if no transactions`() {
        `when`(transactionRepository.findByFromAccountIdAndUserId(anyString(), anyString())).thenReturn(emptyList())
        `when`(transactionRepository.findByToAccountIdAndUserId(anyString(), anyString())).thenReturn(emptyList())
        val result = budgetService.getMonthlyExpensesForAllAccounts("testUser")
        assertTrue(result.totalExpenses == 0.0)
    }

    @Test
    fun `should cache expenses summary`() {
        val result = budgetService.getMonthlyExpensesForAllAccounts("testUser")
        verify(mock(CacheHelperService::class.java), times(1)).cacheMonthlyExpenses(anyString(), any())
    }
}