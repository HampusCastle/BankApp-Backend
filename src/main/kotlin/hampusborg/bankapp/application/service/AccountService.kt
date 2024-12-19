package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.application.dto.response.AccountUpdatedResponse
import hampusborg.bankapp.application.dto.response.WithdrawFundsResponse
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.enums.AccountType
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.util.AccountUtils.mapToAccountDetailsResponse
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.slf4j.LoggerFactory

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val activityLogService: ActivityLogService,
    private val jwtUtil: JwtUtil,
    private val cacheHelperService: CacheHelperService,
    private val paymentService: PaymentService,
) {

    private val log = LoggerFactory.getLogger(AccountService::class.java)

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

        log.info("Attempting to retrieve account by ID: $accountId")

        val accountDetails = cacheHelperService.getAccountsByUserId(userId)
            .find { it.id == accountId }

        if (accountDetails != null) {
            log.info("Account found in cache: $accountId")
            return accountDetails
        }

        val account = accountRepository.findById(accountId).orElseThrow {
            log.error("Account not found for ID: $accountId")
            AccountNotFoundException("Account not found for ID: $accountId")
        }

        if (account.userId != userId) {
            log.error("User does not have permission to view this account: $accountId")
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
            return emptyList()
        }

        cacheHelperService.storeAccountsByUserId(userId, accounts)
        return accounts.map { mapToAccountDetailsResponse(it) }
    }

    fun addFundsToAccount(accountId: String, amount: Double, token: String): AccountUpdatedResponse {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found for ID: $accountId")
        }

        if (account.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Account does not belong to the user.")
        }

        if (amount <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.")
        }

        account.balance += amount
        val savedAccount = accountRepository.save(account)

        // Log transaction
        paymentService.logTransaction(
            fromAccountId = "EXTERNAL_SOURCE",      // Static external source
            toAccountId = savedAccount.id!!,        // Use non-null id after saving
            userId = userId,
            amount = amount,
            category = TransactionCategory.ADDED_FUNDS
        )

        return AccountUpdatedResponse(
            id = savedAccount.id,
            name = savedAccount.name,
            balance = savedAccount.balance,
            message = "Funds added successfully"
        )
    }

    fun withdrawFundsFromAccount(accountId: String, amount: Double, token: String): WithdrawFundsResponse {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found for ID: $accountId")
        }

        if (account.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Account does not belong to the user.")
        }

        if (amount <= 0 || amount > account.balance) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid withdrawal amount.")
        }

        account.balance -= amount
        accountRepository.save(account)

        // Log transaction
        paymentService.logTransaction(
            fromAccountId = account.id!!,
            toAccountId = "EXTERNAL_DESTINATION",
            userId = userId,
            amount = amount,
            category = TransactionCategory.WITHDRAW_FUNDS
        )

        return WithdrawFundsResponse(
            id = account.id,
            name = account.name,
            balance = account.balance,
            message = "Funds withdrawn successfully"
        )
    }

    fun deleteAccount(accountId: String, token: String) {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val account = accountRepository.findById(accountId).orElseThrow {
            AccountNotFoundException("Account not found for ID: $accountId")
        }

        if (account.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Account does not belong to the user.")
        }

        if (account.balance != 0.0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot delete an account with a positive or negative balance."
            )
        }

        accountRepository.delete(account)
        activityLogService.logActivity(userId, "Account deleted", "Account ID: $accountId")

        cacheHelperService.evictCache("userAccounts", userId)
    }
}