package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.AddFundsToAccountRequest
import hampusborg.bankapp.application.dto.request.CreateAccountRequest
import hampusborg.bankapp.application.dto.request.WithdrawFundsRequest
import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.application.dto.response.AccountUpdatedResponse
import hampusborg.bankapp.application.dto.response.WithdrawFundsResponse
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
        val accounts = accountService.getAllAccountsByUser(token)
        return if (accounts.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(accounts)
        }
    }

    @PostMapping("/{accountId}/add-funds")
    fun addFundsToAccount(
        @PathVariable accountId: String,
        @Valid @RequestBody addFundsRequest: AddFundsToAccountRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<AccountUpdatedResponse> {
        return try {
            val response = accountService.addFundsToAccount(accountId, addFundsRequest.amount, token)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    @PostMapping("/{accountId}/withdraw")
    fun withdrawFundsFromAccount(
        @PathVariable accountId: String,
        @Valid @RequestBody withdrawRequest: WithdrawFundsRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<WithdrawFundsResponse> {
        return try {
            val response = accountService.withdrawFundsFromAccount(accountId, withdrawRequest.amount, token)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
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