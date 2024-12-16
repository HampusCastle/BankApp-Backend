package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.dto.response.ScheduledPaymentDetailsResponse
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.stereotype.Service

@Service
class ScheduledPaymentService(
    private val scheduledPaymentRepository: ScheduledPaymentRepository,
    private val jwtUtil: JwtUtil
) {

    fun createScheduledPayment(
        request: CreateScheduledPaymentRequest,
        token: String?
    ): ScheduledPaymentDetailsResponse {
        if (token.isNullOrEmpty()) {
            throw IllegalStateException("Authorization token is missing")
        }

        val userDetails = jwtUtil.extractUserDetails(token.substringAfter(" "))
        val userId = userDetails?.first ?: throw IllegalStateException("User ID could not be extracted from token")

        if (request.schedule !in listOf("daily", "weekly", "monthly")) {
            throw IllegalArgumentException("Invalid schedule type. Must be 'daily', 'weekly', or 'monthly'.")
        }

        val scheduledPayment = ScheduledPayment(
            userId = userId,
            amount = request.amount,
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            schedule = request.schedule,
            nextPaymentDate = request.nextPaymentDate
        )

        val savedPayment = scheduledPaymentRepository.save(scheduledPayment)

        return ScheduledPaymentDetailsResponse(
            message = "Scheduled payment created successfully for user: $userId",
            paymentId = savedPayment.id,
            amount = savedPayment.amount
        )
    }

    fun updateScheduledPayment(
        id: String,
        request: CreateScheduledPaymentRequest
    ): ScheduledPaymentDetailsResponse {
        val existingPayment = scheduledPaymentRepository.findById(id).orElseThrow {
            Exception("Payment not found")
        }

        existingPayment.amount = request.amount
        existingPayment.schedule = request.schedule
        existingPayment.nextPaymentDate = request.nextPaymentDate

        val updatedPayment = scheduledPaymentRepository.save(existingPayment)

        return ScheduledPaymentDetailsResponse(
            message = "Scheduled payment updated successfully.",
            paymentId = updatedPayment.id,
            amount = updatedPayment.amount
        )
    }

    fun getPaymentsDue(currentTime: Long): List<ScheduledPayment> {
        return scheduledPaymentRepository.findAll().filter { it.nextPaymentDate <= currentTime }
    }

    fun save(payment: ScheduledPayment): ScheduledPayment {
        return scheduledPaymentRepository.save(payment)
    }

    fun deleteScheduledPayment(id: String): ScheduledPaymentDetailsResponse {
        val payment = scheduledPaymentRepository.findById(id).orElseThrow {
            Exception("Payment not found")
        }

        scheduledPaymentRepository.delete(payment)

        return ScheduledPaymentDetailsResponse(
            message = "Scheduled payment deleted successfully",
            paymentId = payment.id,
            amount = payment.amount
        )
    }
}