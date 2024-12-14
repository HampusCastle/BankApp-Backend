package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import org.springframework.stereotype.Service

@Service
class ScheduledPaymentService(
    private val scheduledPaymentRepository: ScheduledPaymentRepository
) {
    fun createScheduledPayment(request: CreateScheduledPaymentRequest, userId: String): ScheduledPayment {
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
        val existingPayment = scheduledPaymentRepository.findById(id).orElseThrow {
            Exception("Payment not found")
        }

        existingPayment.amount = request.amount
        existingPayment.schedule = request.schedule
        existingPayment.nextPaymentDate = request.nextPaymentDate

        return scheduledPaymentRepository.save(existingPayment)
    }

    fun deleteScheduledPayment(id: String) {
        val payment = scheduledPaymentRepository.findById(id).orElseThrow {
            Exception("Payment not found")
        }

        scheduledPaymentRepository.delete(payment)
    }

    fun getPaymentsDue(currentTime: Long): List<ScheduledPayment> {
        return scheduledPaymentRepository.findAll().filter { it.nextPaymentDate <= currentTime }
    }

    fun save(payment: ScheduledPayment) {
        scheduledPaymentRepository.save(payment)
    }
}