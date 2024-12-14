package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.application.exception.classes.AccountNotActiveException
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.repository.AccountRepository
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val activityLogService: ActivityLogService,
    private val rateLimiterService: RateLimiterService,
    private val cacheHelperService: CacheHelperService
) {
    fun createAccount(createAccountRequest: CreateAccountRequest, userId: String): AccountDetailsResponse {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        val account = Account(
            userId = userId,
            balance = createAccountRequest.balance,
            accountType = createAccountRequest.accountType
        )

        activityLogService.logActivity(userId, "Account created", "Account Type: ${createAccountRequest.accountType}, Balance: ${createAccountRequest.balance}")
        val savedAccount = accountRepository.save(account)

        return AccountDetailsResponse(
            id = savedAccount.id!!,
            name = savedAccount.accountType,
            balance = savedAccount.balance,
            accountType = savedAccount.accountType,
            userId = savedAccount.userId
        )
    }

    fun getAccountsByUserId(userId: String): List<AccountDetailsResponse> {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }
        val accounts = cacheHelperService.getAccountsByUserId(userId)
        if (accounts.isEmpty()) {
            throw AccountNotFoundException("No accounts found for user ID: $userId")
        }
        return accounts
    }

    fun deleteAccount(accountId: String, userId: String): Boolean {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found")
        }

        if (account.userId == userId) {
            accountRepository.delete(account)
            return true
        } else {
            throw AccountNotActiveException("Account is not active or belongs to a different user.")
        }
    }

    fun getAccountBalance(accountId: String): Double {
        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found")
        }
        return account.balance
    }
}