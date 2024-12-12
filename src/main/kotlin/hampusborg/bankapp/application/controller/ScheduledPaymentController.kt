package hampusborg.bankapp.application.controller

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/scheduled-payments")
class ScheduledPaymentController(
    private val scheduledPaymentService: ScheduledPaymentService,
    private val jwtUtil: JwtUtil
) {

    private val logger = LoggerFactory.getLogger(ScheduledPaymentController::class.java)

    @PostMapping
    fun createScheduledPayment(
        @Valid @RequestBody scheduledPaymentRequest: ScheduledPaymentRequest,
        @RequestHeader("Authorization", required = false) token: String?
    ): ResponseEntity<ScheduledPaymentResponse> {
        if (token.isNullOrEmpty()) {
            return ResponseEntity.status(401).build()
        }

        val userDetails = jwtUtil.extractUserDetails(token.substringAfter(" "))
        val userId = userDetails?.first ?: return ResponseEntity.status(401).build()

        logger.info("Received request to create scheduled payment for user: $userId")

        return try {
            val createdPayment = scheduledPaymentService.createScheduledPayment(scheduledPaymentRequest, userId)

            val response = ScheduledPaymentResponse(
                message = "Scheduled payment created successfully for user: $userId",
                paymentId = createdPayment.id
            )
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error creating scheduled payment: ${e.message}")
            ResponseEntity.badRequest().body(ScheduledPaymentResponse(message = "Error creating scheduled payment"))
        }
    }


    @PutMapping("/{id}")
    fun updateScheduledPayment(
        @PathVariable id: String,
        @Valid @RequestBody scheduledPaymentRequest: ScheduledPaymentRequest
    ): ResponseEntity<ScheduledPaymentResponse> {
        logger.info("Received request to update scheduled payment with ID: $id")
        return try {
            val updatedPayment = scheduledPaymentService.updateScheduledPayment(id, scheduledPaymentRequest)
            val response = ScheduledPaymentResponse(message = "Scheduled payment updated successfully.")
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ScheduledPaymentResponse(message = "Error updating scheduled payment"))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteScheduledPayment(@PathVariable id: String): ResponseEntity<ScheduledPaymentResponse> {
        logger.info("Received request to delete scheduled payment with ID: $id")
        return try {
            scheduledPaymentService.deleteScheduledPayment(id)
            ResponseEntity.ok(ScheduledPaymentResponse(message = "Scheduled payment deleted successfully"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ScheduledPaymentResponse(message = "Error deleting scheduled payment"))
        }
    }
}