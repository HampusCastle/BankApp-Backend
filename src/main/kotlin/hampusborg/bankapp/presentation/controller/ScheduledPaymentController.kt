package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.dto.response.ScheduledPaymentDetailsResponse
import hampusborg.bankapp.application.service.ScheduledPaymentService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/scheduled-payments")
class ScheduledPaymentController(private val scheduledPaymentService: ScheduledPaymentService) {

    @PostMapping
    fun createScheduledPayment(
        @Valid @RequestBody createScheduledPaymentRequest: CreateScheduledPaymentRequest,
        @RequestHeader("Authorization", required = false) token: String?
    ): ResponseEntity<ScheduledPaymentDetailsResponse> {
        val userId = extractUserIdFromToken(token)
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

    private fun extractUserIdFromToken(token: String?): String {
        return "extractedUserId"
    }
}