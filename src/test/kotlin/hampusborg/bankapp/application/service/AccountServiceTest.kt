package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.repository.AccountRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.assertEquals

class AccountServiceTest {

    private val accountRepository: AccountRepository = mock(AccountRepository::class.java)
    private val activityLogService: ActivityLogService = mock(ActivityLogService::class.java)

    private val accountService = AccountService(accountRepository, activityLogService)

    @Test
    fun `createAccount should save the account`() {
        val createAccountRequest = CreateAccountRequest(accountType = "Checking", balance = 1000.0, name = "Checking", userId = "user-id")
        val account = Account(userId = "user-id", accountType = "Checking", balance = 1000.0, interestRate = null, id = "generated-id")

        `when`(accountRepository.save(any())).thenReturn(account)

        val savedAccount = accountService.createAccount(createAccountRequest, "user-id")

        assertEquals("generated-id", savedAccount.id)
        assertEquals(1000.0, savedAccount.balance)
        assertEquals("Checking", savedAccount.accountType)
    }

    @Test
    fun `getAccountsByUserId should return accounts for valid userId`() {
        val account = Account(id = "1", userId = "user-id", accountType = "Checking", balance = 1000.0)

        `when`(accountRepository.findByUserId("user-id")).thenReturn(listOf(account))

        val accounts = accountService.getAccountsByUserId("user-id")

        assertEquals(1, accounts.size)
        assertEquals("Checking", accounts[0].accountType)
    }

    @Test
    fun `should delete account for valid userId`() {
        val account = Account(id = "123", userId = "user-id", accountType = "Checking", balance = 100.0, interestRate = 1.0)
        val accountId = account.id!!
        val userId = account.userId

        `when`(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        doNothing().`when`(accountRepository).delete(account)

        val result = accountService.deleteAccount(accountId, userId)

        assert(result)
        verify(accountRepository, times(1)).delete(account)
    }

    @Test
    fun `createAccount should return bad request when invalid account data is provided`() {
        val createAccountRequest = CreateAccountRequest(accountType = "", balance = -1000.0, name = "", userId = "user-id")

        `when`(accountRepository.save(any())).thenThrow(IllegalArgumentException("Invalid account data"))

        try {
            accountService.createAccount(createAccountRequest, "user-id")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid account data", e.message)
        }
    }

    @Test
    fun `should throw AccountNotFoundException when no accounts found for user`() {
        `when`(accountRepository.findByUserId("non-existing-user")).thenReturn(emptyList())

        val exception = assertThrows<AccountNotFoundException> {
            accountService.getAccountsByUserId("non-existing-user")
        }

        assertEquals("No accounts found for user ID: non-existing-user", exception.message)
    }
}