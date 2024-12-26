package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.dto.response.ScheduledPaymentDetailsResponse
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import org.springframework.stereotype.Service

@Service
class ScheduledPaymentService(private val scheduledPaymentRepository: ScheduledPaymentRepository) {

    fun createScheduledPayment(
        request: CreateScheduledPaymentRequest,
        userId: String
    ): ScheduledPaymentDetailsResponse {
        require(
            listOf("daily", "weekly", "monthly").contains(request.schedule)
        ) { "Invalid schedule type. Must be 'daily', 'weekly', or 'monthly'." }

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

    fun updateScheduledPayment(id: String, request: CreateScheduledPaymentRequest): ScheduledPaymentDetailsResponse {
        val existingPayment = scheduledPaymentRepository.findById(id).orElseThrow {
            RuntimeException("Payment not found")
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

    fun deleteScheduledPayment(id: String): ScheduledPaymentDetailsResponse {
        val payment = scheduledPaymentRepository.findById(id).orElseThrow {
            RuntimeException("Payment not found")
        }
        scheduledPaymentRepository.delete(payment)
        return ScheduledPaymentDetailsResponse("Scheduled payment deleted successfully", payment.id, payment.amount)
    }

    fun getPaymentsDue(currentTime: Long): List<ScheduledPayment> {
        return scheduledPaymentRepository.findAll()
            .filter { it.nextPaymentDate <= currentTime }
    }

    fun save(payment: ScheduledPayment): ScheduledPayment {
        return scheduledPaymentRepository.save(payment)
    }

    fun getScheduledPaymentsByUser(userId: String?): List<ScheduledPayment> {
        return scheduledPaymentRepository.findByUserId(userId!!)
    }
}