package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.application.service.AccountService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService,
    private val jwtUtil: JwtUtil
) {
    private val logger = LoggerFactory.getLogger(AccountController::class.java)

    @PostMapping("/create")
    fun createAccount(
        @Valid @RequestBody createAccountRequest: CreateAccountRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AccountDetailsResponse> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        logger.info("Creating account for user: $userId")
        return try {
            val accountResponse = accountService.createAccount(createAccountRequest.copy(userId = userId), userId)
            logger.info("Account created successfully for user $userId")
            ResponseEntity.ok(accountResponse)
        } catch (e: Exception) {
            logger.error("Error creating account for user $userId: ${e.message}")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    @GetMapping("/{accountId}")
    fun getAccountById(
        @PathVariable accountId: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AccountDetailsResponse> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        logger.info("Fetching account with ID $accountId for user $userId")
        val account = accountService.getAccountsByUserId(userId).find { it.id == accountId }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")

        return ResponseEntity.ok(account)
    }

    @GetMapping("/my-accounts")
    fun getAllAccounts(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<AccountDetailsResponse>> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        logger.info("Fetching all accounts for user: $userId")
        val accounts = accountService.getAccountsByUserId(userId)
        if (accounts.isEmpty()) {
            logger.warn("No accounts found for user: $userId")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No accounts found")
        }
        return ResponseEntity.ok(accounts)
    }

    @DeleteMapping("/{accountId}")
    fun deleteAccount(
        @PathVariable accountId: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<String> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

        logger.info("Deleting account with ID $accountId for user $userId")
        return if (accountService.deleteAccount(accountId, userId)) {
            logger.info("Account with ID $accountId deleted successfully")
            ResponseEntity.ok("Account deleted successfully")
        } else {
            logger.warn("Account with ID $accountId not found for user $userId")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")
        }
    }
}
