package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.exception.classes.NoTransactionsFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class BudgetServiceTest {

    private val transactionRepository: TransactionRepository = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val accountRepository: AccountRepository = mock()
    private val budgetService = BudgetService(transactionRepository, cacheHelperService, accountRepository)

    @Test
    fun `should return cached monthly expenses`() {
        val cachedResponse = ExpensesSummaryResponse(100.0, mapOf("SUBSCRIPTION" to 100.0))

        whenever(cacheHelperService.getMonthlyExpenses("user:testUser")).thenReturn(cachedResponse)

        val result = budgetService.getMonthlyExpensesForAllAccounts("testUser")

        assertEquals(100.0, result.totalExpenses)
        assertEquals(mapOf("SUBSCRIPTION" to 100.0), result.categories)
        verify(cacheHelperService, times(1)).getMonthlyExpenses("user:testUser")
    }

    @Test
    fun `should throw exception when no transactions found`() {
        whenever(accountRepository.findByUserId("testUser")).thenReturn(emptyList())

        assertThrows(NoTransactionsFoundException::class.java) {
            budgetService.getMonthlyExpensesForAllAccounts("testUser")
        }
    }

    @Test
    fun `should calculate and cache monthly expenses`() {
        val transactions = listOf(
            Transaction(
                amount = 50.0,
                userId = "testUser",
                fromAccountId = "account1",
                toAccountId = "account2",
                categoryId = TransactionCategory.SUBSCRIPTION,
                date = LocalDateTime.now(),
                timestamp = System.currentTimeMillis()
            )
        )

        whenever(accountRepository.findByUserId("testUser")).thenReturn(emptyList())
        whenever(transactionRepository.findByFromAccountIdAndUserId(any(), any())).thenReturn(transactions)
        whenever(transactionRepository.findByToAccountIdAndUserId(any(), any())).thenReturn(emptyList())

        val result = budgetService.getMonthlyExpensesForAllAccounts("testUser")

        assertEquals(50.0, result.totalExpenses)
        assertEquals(mapOf("SUBSCRIPTION" to 50.0), result.categories)
        verify(cacheHelperService, times(1)).cacheMonthlyExpenses(eq("user:testUser"), any())
    }
}