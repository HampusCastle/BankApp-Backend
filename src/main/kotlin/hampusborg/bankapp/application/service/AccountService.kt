package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.application.exception.classes.AccountNotActiveException
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val activityLogService: ActivityLogService
) {
    private val logger = LoggerFactory.getLogger(AccountService::class.java)

    fun createAccount(createAccountRequest: CreateAccountRequest, userId: String): AccountDetailsResponse {
        logger.info("Creating account for userId: $userId with accountType: ${createAccountRequest.accountType} and balance: ${createAccountRequest.balance}")
        val account = Account(
            userId = userId,
            balance = createAccountRequest.balance,
            accountType = createAccountRequest.accountType
        )

        activityLogService.logActivity(userId, "Account created", "Account Type: ${createAccountRequest.accountType}, Balance: ${createAccountRequest.balance}")
        val savedAccount = accountRepository.save(account)
        logger.info("Account created successfully: ${savedAccount.id}")

        return AccountDetailsResponse(
            id = savedAccount.id!!,
            name = savedAccount.accountType,
            balance = savedAccount.balance,
            accountType = savedAccount.accountType,
            userId = savedAccount.userId
        )
    }

    fun getAccountsByUserId(userId: String): List<AccountDetailsResponse> {
        logger.info("Fetching accounts for userId: $userId")
        return accountRepository.findByUserId(userId).map { account ->
            AccountDetailsResponse(
                id = account.id!!,
                name = account.accountType,
                balance = account.balance,
                accountType = account.accountType,
                userId = account.userId
            )
        }.takeIf { it.isNotEmpty() } ?: throw AccountNotFoundException("No accounts found for user ID: $userId")
    }

    fun deleteAccount(accountId: String, userId: String): Boolean {
        logger.info("Deleting account with id: $accountId for userId: $userId")
        val account = accountRepository.findById(accountId).orElseThrow { AccountNotFoundException("Account not found") }
        return if (account.userId == userId) {
            accountRepository.delete(account)
            logger.info("Account deleted successfully: $accountId")
            true
        } else {
            throw AccountNotActiveException("Account is not active or belongs to a different user.")
        }
    }

    fun getAccountBalance(accountId: String): Double {
        val account = accountRepository.findById(accountId)
            .orElseThrow { AccountNotFoundException("Account not found") }
        return account.balance
    }
}