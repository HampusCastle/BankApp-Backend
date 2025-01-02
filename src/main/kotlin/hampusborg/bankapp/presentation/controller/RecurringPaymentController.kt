package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.dto.response.RecurringPaymentResponse
import hampusborg.bankapp.application.service.RecurringPaymentService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/recurring-payments")
class RecurringPaymentController(
    private val recurringPaymentService: RecurringPaymentService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRecurringPayment(@RequestBody request: RecurringPaymentRequest): RecurringPaymentResponse {
        return recurringPaymentService.createRecurringPayment(request)
    }

    @GetMapping
    fun getRecurringPayments(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<RecurringPaymentResponse>> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")
        val payments = recurringPaymentService.getAllRecurringPayments(userId)
        return ResponseEntity.ok(payments)
    }

    @PutMapping("/{paymentId}")
    fun updateRecurringPayment(
        @PathVariable paymentId: String,
        @RequestBody request: RecurringPaymentRequest
    ): RecurringPaymentResponse {
        return recurringPaymentService.updateRecurringPayment(paymentId, request)
    }

    @DeleteMapping("/{paymentId}")
    fun cancelRecurringPayment(@PathVariable paymentId: String): String {
        recurringPaymentService.cancelRecurringPayment(paymentId)
        return "Recurring payment with ID $paymentId has been canceled."
    }
}
