package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.dto.response.ScheduledPaymentDetailsResponse
import hampusborg.bankapp.application.service.ScheduledPaymentService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/scheduled-payments")
class ScheduledPaymentController(
    private val scheduledPaymentService: ScheduledPaymentService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping
    fun createScheduledPayment(
        @Valid @RequestBody createScheduledPaymentRequest: CreateScheduledPaymentRequest,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ScheduledPaymentDetailsResponse> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")
        println("Received Scheduled Payment Request: $createScheduledPaymentRequest")
        return ResponseEntity.ok(scheduledPaymentService.createScheduledPayment(createScheduledPaymentRequest, userId))
    }

    @PutMapping("/{id}")
    fun updateScheduledPayment(
        @PathVariable id: String,
        @Valid @RequestBody createScheduledPaymentRequest: CreateScheduledPaymentRequest
    ): ResponseEntity<ScheduledPaymentDetailsResponse> {
        return ResponseEntity.ok(scheduledPaymentService.updateScheduledPayment(id, createScheduledPaymentRequest))
    }

    @DeleteMapping("/{id}")
    fun deleteScheduledPayment(@PathVariable id: String): ResponseEntity<ScheduledPaymentDetailsResponse> {
        return ResponseEntity.ok(scheduledPaymentService.deleteScheduledPayment(id))
    }

    @GetMapping
    fun getScheduledPayments(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<ScheduledPaymentDetailsResponse>> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")
        return ResponseEntity.ok(scheduledPaymentService.getScheduledPaymentsByUserId(userId))
    }
}