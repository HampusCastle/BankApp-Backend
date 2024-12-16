package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.application.exception.classes.AccountNotActiveException
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.enums.AccountType
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.util.AccountUtils.mapToAccountDetailsResponse
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val activityLogService: ActivityLogService,
    private val jwtUtil: JwtUtil,
    private val cacheHelperService: CacheHelperService
) {

    fun createAccountWithUserValidation(createAccountRequest: CreateAccountRequest, token: String): AccountDetailsResponse {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        return createAccount(createAccountRequest, userId)
    }

    private fun createAccount(createAccountRequest: CreateAccountRequest, userId: String): AccountDetailsResponse {
        val account = Account(
            userId = userId,
            name = createAccountRequest.name,
            balance = createAccountRequest.balance,
            accountType = AccountType.valueOf(createAccountRequest.accountType.uppercase())
        )

        val savedAccount = accountRepository.save(account)

        activityLogService.logActivity(
            userId,
            "Account created",
            "Account Type: ${createAccountRequest.accountType}, Balance: ${createAccountRequest.balance}"
        )

        cacheHelperService.storeAccountsByUserId(userId, listOf(savedAccount))
        return mapToAccountDetailsResponse(savedAccount)
    }

    fun getAccountById(accountId: String, token: String): AccountDetailsResponse {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val accountDetails = cacheHelperService.getAccountsByUserId(userId)
            .find { it.id == accountId }

        if (accountDetails != null) {
            return accountDetails
        }

        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found for ID: $accountId")
        }

        if (account.userId != userId) {
            throw IllegalStateException("User does not have permission to view this account")
        }

        cacheHelperService.storeAccountsByUserId(userId, listOf(account))

        return mapToAccountDetailsResponse(account)
    }

    fun getAccountBalance(accountId: String, userId: String): Double {
        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found for ID: $accountId")
        }

        if (account.userId != userId) {
            throw IllegalStateException("User does not have permission to view the balance for this account.")
        }

        return account.balance
    }

    fun getAllAccountsByUser(token: String): List<AccountDetailsResponse> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val cachedAccounts = cacheHelperService.getAccountsByUserId(userId)
        if (cachedAccounts.isNotEmpty()) {
            return cachedAccounts
        }

        val accounts = accountRepository.findByUserId(userId)
        if (accounts.isEmpty()) {
            throw AccountNotFoundException("No accounts found for user ID: $userId")
        }

        cacheHelperService.storeAccountsByUserId(userId, accounts)

        return accounts.map {mapToAccountDetailsResponse(it) }
    }

    fun deleteAccount(accountId: String, token: String) {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found for ID: $accountId")
        }

        if (account.userId != userId) {
            throw AccountNotActiveException("Account does not belong to the user or is inactive.")
        }

        if (account.balance > 0) {
            throw IllegalStateException("Account cannot be deleted because it has a positive balance.")
        }

        accountRepository.delete(account)
        activityLogService.logActivity(userId, "Account deleted", "Account ID: $accountId")

        cacheHelperService.evictCache("userAccounts", userId)
    }

}