package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.repository.AccountRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest
class InterestCalculatorServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val interestCalculatorService = InterestCalculatorService(accountRepository)

    @Test
    fun `should apply interest to savings accounts`() {
        val account = Account(
            id = "1",
            accountType = "Savings",
            balance = 1000.0,
            userId = "user123",
            interestRate = 5.0
        )

        whenever(accountRepository.findByAccountType("Savings")).thenReturn(listOf(account))

        interestCalculatorService.applyInterest()

        val expectedBalance = 1000.0 * 1.05

        verify(accountRepository).save(account)
        assertEquals(expectedBalance, account.balance)
    }

    @Test
    fun `should handle failure when saving account with interest`() {
        val account = Account(
            id = "1",
            accountType = "Savings",
            balance = 1000.0,
            userId = "user123",
            interestRate = 0.05
        )

        whenever(accountRepository.findByAccountType("Savings")).thenReturn(listOf(account))
        whenever(accountRepository.save(account)).thenThrow(RuntimeException("Database error"))

        try {
            interestCalculatorService.applyInterest()
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }

    @Test
    fun `should apply interest only to savings accounts`() {
        val account1 = Account(
            id = "1",
            accountType = "Savings",
            balance = 1000.0,
            userId = "user123",
            interestRate = 5.0
        )
        val account2 = Account(
            id = "2",
            accountType = "Checking",
            balance = 5000.0,
            userId = "user123",
            interestRate = 0.0
        )

        whenever(accountRepository.findByAccountType("Savings")).thenReturn(listOf(account1))

        interestCalculatorService.applyInterest()

        val expectedBalance1 = 1000.0 * 1.05

        assertEquals(expectedBalance1, account1.balance)
        verify(accountRepository, times(1)).save(account1)

        verify(accountRepository, never()).save(account2)
    }
}