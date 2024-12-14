package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateTransactionRequest
import hampusborg.bankapp.application.mapper.TransactionMapper
import hampusborg.bankapp.application.service.TransactionService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val jwtUtil: JwtUtil,
    private val transactionMapper: TransactionMapper
) {
    @PostMapping
    fun createTransaction(
        @Valid @RequestBody createTransactionRequest: CreateTransactionRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Any> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
        return if (userId != null) {
            val transaction = transactionMapper.mapToTransaction(createTransactionRequest)
            transaction.id = "generatedId"
            ResponseEntity.ok(mapOf("message" to "Transaction created successfully", "transactionId" to transaction.id))
        } else {
            ResponseEntity.badRequest().body("User ID could not be extracted from token")
        }
    }

    @GetMapping("/history")
    fun getTransactionHistory(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Any> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
        return if (userId != null) {
            val transactions = transactionService.getTransactionHistory(userId)
            ResponseEntity.ok(transactions)
        } else {
            ResponseEntity.badRequest().body("User ID could not be extracted from token")
        }
    }
}