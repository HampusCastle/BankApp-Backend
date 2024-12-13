package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.TransactionRequest
import hampusborg.bankapp.application.mapper.TransactionMapper
import hampusborg.bankapp.application.service.TransactionService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val jwtUtil: JwtUtil,
    private val transactionMapper: TransactionMapper
) {

    private val logger = LoggerFactory.getLogger(TransactionController::class.java)

    @PostMapping
    fun createTransaction(
        @Valid @RequestBody transactionRequest: TransactionRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<Any> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
        return if (userId != null) {
            logger.info("Creating transaction for user: $userId")
            val transaction = transactionMapper.mapToTransaction(transactionRequest)
            transaction.id = "generatedId"
            ResponseEntity.ok(
                mapOf("message" to "Transaction created successfully", "transactionId" to transaction.id)
            )
        } else {
            logger.error("User ID could not be extracted from token")
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
