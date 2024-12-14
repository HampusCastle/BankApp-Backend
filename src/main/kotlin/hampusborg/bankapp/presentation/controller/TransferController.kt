package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.response.TransferStatusResponse
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
        @Valid @RequestBody initiateTransferRequest: InitiateTransferRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<TransferStatusResponse> {
        println("Received transfer request: $initiateTransferRequest")
        val userDetails = jwtUtil.extractUserDetails(token.substringAfter(" "))
        val userId = userDetails?.first

        return if (userId != null) {
            return try {
                val transferResponse = transferService.transferFunds(initiateTransferRequest, userId)
                ResponseEntity.ok(transferResponse)
            } catch (e: RuntimeException) {
                ResponseEntity.badRequest().body(TransferStatusResponse(message = "Transfer failed", status = "failed"))
            }
        } else {
            ResponseEntity.badRequest().body(TransferStatusResponse(message = "User ID could not be extracted from token"))
        }
    }
}
