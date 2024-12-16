package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.application.service.AccountService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import jakarta.validation.Valid
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

    @PostMapping("/create")
    fun createAccount(
        @Valid @RequestBody createAccountRequest: CreateAccountRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AccountDetailsResponse> {
        return try {
            val accountResponse = accountService.createAccountWithUserValidation(createAccountRequest, token)
            ResponseEntity.ok(accountResponse)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    @GetMapping("/{accountId}")
    fun getAccountById(
        @PathVariable accountId: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AccountDetailsResponse> {
        return try {
            val account = accountService.getAccountById(accountId, token)
            ResponseEntity.ok(account)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")
        }
    }

    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @PathVariable accountId: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Double> {
        return try {
            val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")

            val balance = accountService.getAccountBalance(accountId, userId)
            ResponseEntity.ok(balance)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or permission denied")
        }
    }

    @GetMapping("/my-accounts")
    fun getAllAccounts(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<AccountDetailsResponse>> {
        return try {
            val accounts = accountService.getAllAccountsByUser(token)
            ResponseEntity.ok(accounts)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No accounts found")
        }
    }

    @DeleteMapping("/{accountId}")
    fun deleteAccount(
        @PathVariable accountId: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<String> {
        return try {
            accountService.deleteAccount(accountId, token)
            ResponseEntity.ok("Account deleted successfully")
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")
        }
    }
}