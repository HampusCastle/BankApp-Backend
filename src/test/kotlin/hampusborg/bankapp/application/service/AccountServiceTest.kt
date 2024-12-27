import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.enums.AccountType
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.application.service.AccountService
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.application.service.ActivityLogService
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.Optional

class AccountServiceTest {

    private val accountRepository: AccountRepository = mock(AccountRepository::class.java)
    private val accountService: AccountService = AccountService(
        accountRepository = accountRepository,
        activityLogService = mock(ActivityLogService::class.java),
        jwtUtil = mock(JwtUtil::class.java),
        cacheHelperService = mock(CacheHelperService::class.java),
        paymentService = mock(PaymentService::class.java)
    )

    @Test
    fun `should create account successfully`() {
        val account = Account(
            id = "1",
            userId = "testUser",
            name = "Test Account",
            balance = 1000.0,
            accountType = AccountType.CHECKING
        )
        `when`(accountRepository.save(any(Account::class.java))).thenReturn(account)
        val createdAccount = accountService.createAccountWithUserValidation(
            CreateAccountRequest("Test Account", 1000.0, "CHECKING"),
            "testToken"
        )
        assertEquals("Test Account", createdAccount.name)
    }

    @Test
    fun `should throw AccountNotFoundException when account not found`() {
        `when`(accountRepository.findById("invalidId")).thenReturn(Optional.empty())
        assertThrows(AccountNotFoundException::class.java) {
            accountService.getAccountById("invalidId", "testToken")
        }
    }
}