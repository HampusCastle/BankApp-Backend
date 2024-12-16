package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.dto.response.RecurringPaymentResponse
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.RecurringPayment
import hampusborg.bankapp.core.repository.RecurringPaymentRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class RecurringPaymentService(
    private val recurringPaymentRepository: RecurringPaymentRepository,
    private val paymentService: PaymentService,
) {

    fun createRecurringPayment(request: RecurringPaymentRequest): RecurringPaymentResponse {
        val userId = request.userId



        val recurringPayment = RecurringPayment(
            userId = userId,
            amount = request.amount,
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            interval = request.interval,
            categoryId = request.categoryId,
            nextPaymentDate = System.currentTimeMillis(),
            status = "active"
        )

        val savedPayment = recurringPaymentRepository.save(recurringPayment)

        return mapToResponse(savedPayment)
    }

    fun getRecurringPaymentById(id: String): RecurringPaymentResponse {
        val payment = recurringPaymentRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("Recurring payment not found for ID: $id")
        }
        return mapToResponse(payment)
    }

    fun updateRecurringPayment(paymentId: String, request: RecurringPaymentRequest): RecurringPaymentResponse {
        val userId = request.userId



        val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
            throw IllegalArgumentException("Recurring payment not found")
        }

        recurringPayment.amount = request.amount
        recurringPayment.interval = request.interval
        recurringPayment.toAccountId = request.toAccountId
        recurringPayment.categoryId = request.categoryId

        val updatedPayment = recurringPaymentRepository.save(recurringPayment)

        return mapToResponse(updatedPayment)
    }

    fun cancelRecurringPayment(paymentId: String) {
        val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
            throw IllegalArgumentException("Recurring payment not found")
        }
        recurringPayment.status = "canceled"
        recurringPaymentRepository.save(recurringPayment)
    }

    fun getRecurringPaymentsByUserId(userId: String): List<RecurringPaymentResponse> {
        val payments = recurringPaymentRepository.findByUserId(userId)
        return payments.map { mapToResponse(it) }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun processDueRecurringPayments() {
        val now = System.currentTimeMillis()
        val paymentsDue = recurringPaymentRepository.findByNextPaymentDateBeforeAndStatus(now, "active")

        paymentsDue.forEach { recurringPayment ->
            processPayment(recurringPayment)
        }
    }

    private fun processPayment(recurringPayment: RecurringPayment) {
        val transferRequest = createTransferRequest(recurringPayment)
        paymentService.handleTransfer(transferRequest, recurringPayment.userId)
        recurringPayment.nextPaymentDate = calculateNextPaymentDate(recurringPayment.interval)
        recurringPaymentRepository.save(recurringPayment)
    }

    private fun createTransferRequest(recurringPayment: RecurringPayment): InitiateTransferRequest {
        return InitiateTransferRequest(
            fromAccountId = recurringPayment.fromAccountId,
            toAccountId = recurringPayment.toAccountId,
            amount = recurringPayment.amount,
            categoryId = recurringPayment.categoryId
        )
    }

    private fun calculateNextPaymentDate(interval: String): Long {
        val calendar = Calendar.getInstance()
        when (interval) {
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            else -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun mapToResponse(payment: RecurringPayment): RecurringPaymentResponse {
        return RecurringPaymentResponse(
            id = payment.id ?: "",
            userId = payment.userId,
            amount = payment.amount,
            fromAccountId = payment.fromAccountId,
            toAccountId = payment.toAccountId,
            interval = payment.interval,
            status = payment.status,
            categoryId = payment.categoryId,
            nextPaymentDate = payment.nextPaymentDate
        )
    }
}