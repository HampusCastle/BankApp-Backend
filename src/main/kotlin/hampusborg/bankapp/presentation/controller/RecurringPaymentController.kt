package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.dto.response.RecurringPaymentResponse
import hampusborg.bankapp.application.service.RecurringPaymentService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/recurring-payments")
class RecurringPaymentController(
    private val recurringPaymentService: RecurringPaymentService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRecurringPayment(@RequestBody request: RecurringPaymentRequest): RecurringPaymentResponse {
        return recurringPaymentService.createRecurringPayment(request)
    }

    @GetMapping("/{userId}")
    fun getRecurringPayments(@PathVariable userId: String): List<RecurringPaymentResponse> {
        return recurringPaymentService.getRecurringPaymentsByUserId(userId)
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