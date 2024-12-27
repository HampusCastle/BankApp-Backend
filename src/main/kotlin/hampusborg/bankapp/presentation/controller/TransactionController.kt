package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.TransactionDetailsResponse
import hampusborg.bankapp.infrastructure.util.JwtUtil
import hampusborg.bankapp.application.service.TransactionService
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import java.time.LocalDate

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val jwtUtil: JwtUtil
) {
    private val logger = LoggerFactory.getLogger(TransactionController::class.java)

    @GetMapping("/history")
    fun getTransactionHistory(
        @RequestHeader("Authorization") token: String,
        @RequestParam("accountId") accountId: String,
        @RequestParam(required = false) fromDate: LocalDate?,
        @RequestParam(required = false) toDate: LocalDate?,
        @RequestParam(required = false) category: TransactionCategory?,
        @RequestParam(required = false) minAmount: Double?,
        @RequestParam(required = false) maxAmount: Double?
    ): ResponseEntity<List<TransactionDetailsResponse>> {
        logger.info("Received request to fetch transaction history for accountId: $accountId")

        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: return ResponseEntity.badRequest().body(emptyList<TransactionDetailsResponse>()).also {
                logger.warn("User ID could not be extracted from the token.")
            }

        if (accountId.isBlank()) {
            return ResponseEntity.badRequest().body(emptyList<TransactionDetailsResponse>()).also {
                logger.warn("AccountId is blank.")
            }
        }

        try {
            logger.info("Filtering transactions for userId: $userId with fromDate: $fromDate, toDate: $toDate, category: $category, minAmount: $minAmount, maxAmount: $maxAmount")

            val transactions = transactionService.getFilteredTransactions(
                userId, fromDate, toDate, category, minAmount, maxAmount, accountId
            )

            logger.info("Successfully fetched ${transactions.size} transactions for accountId: $accountId")

            val response = transactions.map {
                TransactionDetailsResponse(
                    fromAccountId = it.fromAccountId,
                    toAccountId = it.toAccountId,
                    amount = it.amount,
                    timestamp = it.timestamp,
                    categoryId = it.categoryId.name
                )
            }

            return ResponseEntity.ok(response)

        } catch (e: Exception) {
            logger.error("Error fetching transaction history: ${e.message}", e)
            return ResponseEntity.status(500).body(emptyList())
        }
    }
}