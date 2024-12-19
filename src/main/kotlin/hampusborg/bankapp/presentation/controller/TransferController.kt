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
        @Valid @RequestBody transferRequest: InitiateTransferRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<TransferStatusResponse> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val response = transferService.transferFunds(transferRequest, userId)
        return ResponseEntity.ok(response)
    }
}