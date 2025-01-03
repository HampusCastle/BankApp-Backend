package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.enums.AccountType
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*

class AccountServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val activityLogService: ActivityLogService = mock()
    private val jwtUtil: JwtUtil = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val paymentService: PaymentService = mock()

    private val accountService = AccountService(
        accountRepository,
        activityLogService,
        jwtUtil,
        cacheHelperService,
        paymentService
    )

    @Test
    fun `should create an account successfully`() {
        val createAccountRequest = CreateAccountRequest("Savings", 1000.0, "SAVINGS")
        val account = Account(userId = "testUser", name = "Savings", balance = 1000.0, accountType = AccountType.SAVINGS)

        whenever(accountRepository.save(any())).thenReturn(account)
        whenever(jwtUtil.extractUserDetails(any())).thenReturn(Pair("testUser", listOf("ROLE_USER")))

        val response = accountService.createAccountWithUserValidation(createAccountRequest, "Bearer token")

        assertEquals("Savings", response.name)
        assertEquals(1000.0, response.balance)
        verify(accountRepository, times(1)).save(any())
    }

    @Test
    fun `should throw AccountNotFoundException when account not found`() {
        whenever(accountRepository.findById("invalidAccountId")).thenReturn(Optional.empty())

        assertThrows(AccountNotFoundException::class.java) {
            accountService.getAccountById("invalidAccountId", "Bearer token")
        }
    }

    @Test
    fun `should delete account successfully`() {
        val account = Account(userId = "testUser", name = "Savings", balance = 0.0, accountType = AccountType.SAVINGS)

        whenever(accountRepository.findById("accountId")).thenReturn(Optional.of(account))

        accountService.deleteAccount("accountId", "testUser")

        verify(accountRepository, times(1)).delete(account)
        verify(activityLogService, times(1)).logActivity("testUser", "Account deleted", "Account ID: accountId")
    }
}
