package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.dto.response.ScheduledPaymentDetailsResponse
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import org.springframework.stereotype.Service

@Service
class ScheduledPaymentService(
    private val scheduledPaymentRepository: ScheduledPaymentRepository,
    private val cacheHelperService: CacheHelperService
) {

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
        println("Saving ScheduledPayment: $scheduledPayment")
        val savedPayment = scheduledPaymentRepository.save(scheduledPayment)

        cacheHelperService.evictCache("scheduledPayments", userId)
        cacheHelperService.storeScheduledPayment(savedPayment.id!!, savedPayment)
        cacheHelperService.storeScheduledPaymentsForUser(userId, listOf(savedPayment))

        return mapToResponse(savedPayment, "Scheduled payment created successfully for user: $userId")
    }

    fun updateScheduledPayment(id: String, request: CreateScheduledPaymentRequest): ScheduledPaymentDetailsResponse {
        val existingPayment = scheduledPaymentRepository.findById(id).orElseThrow {
            RuntimeException("Payment not found")
        }

        existingPayment.amount = request.amount
        existingPayment.schedule = request.schedule
        existingPayment.nextPaymentDate = request.nextPaymentDate

        val updatedPayment = scheduledPaymentRepository.save(existingPayment)

        cacheHelperService.storeScheduledPayment(updatedPayment.id!!, updatedPayment)
        cacheHelperService.evictScheduledPaymentsCache(existingPayment.userId)

        val payments = scheduledPaymentRepository.findByUserId(existingPayment.userId)
        cacheHelperService.storeScheduledPaymentsForUser(existingPayment.userId, payments)

        return mapToResponse(updatedPayment, "Scheduled payment updated successfully.")
    }

    fun deleteScheduledPayment(id: String): ScheduledPaymentDetailsResponse {
        val payment = scheduledPaymentRepository.findById(id).orElseThrow {
            RuntimeException("Payment not found")
        }

        scheduledPaymentRepository.delete(payment)

        cacheHelperService.evictScheduledPaymentsCache(payment.userId)
        cacheHelperService.evictCache("scheduledPaymentDetails", id)

        val payments = scheduledPaymentRepository.findByUserId(payment.userId)
        cacheHelperService.storeScheduledPaymentsForUser(payment.userId, payments)

        return mapToResponse(payment, "Scheduled payment deleted successfully.")
    }

    fun getScheduledPaymentsByUserId(userId: String): List<ScheduledPaymentDetailsResponse> {
        val payments = scheduledPaymentRepository.findByUserId(userId)
        return payments.map {
            ScheduledPaymentDetailsResponse(
                message = "Scheduled payment fetched successfully",
                paymentId = it.id,
                fromAccountId = it.fromAccountId,
                toAccountId = it.toAccountId,
                amount = it.amount,
                nextPaymentDate = it.nextPaymentDate,
                schedule = it.schedule
            )
        }
    }

    private fun mapToResponse(
        payment: ScheduledPayment,
        message: String = "Operation successful."
    ): ScheduledPaymentDetailsResponse {
        return ScheduledPaymentDetailsResponse(
            message = message,
            paymentId = payment.id,
            fromAccountId = payment.fromAccountId,
            toAccountId = payment.toAccountId,
            amount = payment.amount,
            nextPaymentDate = payment.nextPaymentDate,
            schedule = payment.schedule
        )
    }
}