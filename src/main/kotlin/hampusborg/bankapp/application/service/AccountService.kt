package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.AccountRequest
import hampusborg.bankapp.application.dto.response.AccountResponse
import hampusborg.bankapp.application.exception.classes.AccountNotActiveException
import hampusborg.bankapp.application.exception.classes.AccountNotFoundException
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
    private val userActivityLogService: UserActivityLogService
) {
    private val logger = LoggerFactory.getLogger(AccountService::class.java)

    fun createAccount(accountRequest: AccountRequest, userId: String): AccountResponse {
        logger.info("Creating account for userId: $userId with accountType: ${accountRequest.accountType} and balance: ${accountRequest.balance}")
        val account = Account(
            userId = userId,
            balance = accountRequest.balance,
            accountType = accountRequest.accountType
        )

        userActivityLogService.logActivity(userId, "Account created", "Account Type: ${accountRequest.accountType}, Balance: ${accountRequest.balance}")
        val savedAccount = accountRepository.save(account)
        logger.info("Account created successfully: ${savedAccount.id}")

        return AccountResponse(
            id = savedAccount.id!!,
            name = savedAccount.accountType,
            balance = savedAccount.balance,
            accountType = savedAccount.accountType,
            userId = savedAccount.userId
        )
    }

    fun getAccountsByUserId(userId: String): List<AccountResponse> {
        logger.info("Fetching accounts for userId: $userId")
        return accountRepository.findByUserId(userId).map { account ->
            AccountResponse(
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