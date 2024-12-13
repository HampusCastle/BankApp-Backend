package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.TransferRequest
import hampusborg.bankapp.application.dto.response.TransferResponse
import hampusborg.bankapp.application.service.TransferService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transfers")
class TransferController(
    private val transferService: TransferService,
    private val jwtUtil: JwtUtil
) {
    @PostMapping
    fun transferFunds(
        @Valid @RequestBody transferRequest: TransferRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<TransferResponse> {
        println("Received transfer request: $transferRequest")
        val userDetails = jwtUtil.extractUserDetails(token.substringAfter(" "))
        val userId = userDetails?.first

        return if (userId != null) {
            return try {
                val transferResponse = transferService.transferFunds(transferRequest, userId)
                ResponseEntity.ok(transferResponse)
            } catch (e: RuntimeException) {
                ResponseEntity.badRequest().body(TransferResponse(message = "Transfer failed", status = "failed"))
            }
        } else {
            ResponseEntity.badRequest().body(TransferResponse(message = "User ID could not be extracted from token"))
        }
    }
}
