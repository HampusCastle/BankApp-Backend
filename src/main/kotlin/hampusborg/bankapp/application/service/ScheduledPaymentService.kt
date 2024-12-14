package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ScheduledPaymentService(
    private val scheduledPaymentRepository: ScheduledPaymentRepository
) {

    private val logger = LoggerFactory.getLogger(ScheduledPaymentService::class.java)

    fun createScheduledPayment(request: CreateScheduledPaymentRequest, userId: String): ScheduledPayment {
        logger.info("Creating scheduled payment for user: $userId with amount: ${request.amount}")

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
        return scheduledPaymentRepository.save(scheduledPayment)
    }

    fun updateScheduledPayment(id: String, request: CreateScheduledPaymentRequest): ScheduledPayment {
        logger.info("Updating scheduled payment with ID: $id")
        val existingPayment = scheduledPaymentRepository.findById(id)
            .orElseThrow { Exception("Payment not found") }

        existingPayment.amount = request.amount
        existingPayment.schedule = request.schedule
        existingPayment.nextPaymentDate = request.nextPaymentDate

        return scheduledPaymentRepository.save(existingPayment)
    }

    fun deleteScheduledPayment(id: String) {
        logger.info("Deleting scheduled payment with ID: $id")
        val payment = scheduledPaymentRepository.findById(id)
            .orElseThrow { Exception("Payment not found") }
        scheduledPaymentRepository.delete(payment)
    }

    fun getPaymentsDue(currentTime: Long): List<ScheduledPayment> {
        logger.info("Fetching payments due at $currentTime")
        return scheduledPaymentRepository.findAll().filter { it.nextPaymentDate <= currentTime }
    }

    fun save(payment: ScheduledPayment) {
        logger.info("Saving scheduled payment with ID: ${payment.id}")
        scheduledPaymentRepository.save(payment)
    }
}