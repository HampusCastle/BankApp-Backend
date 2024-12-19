package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.dto.response.RecurringPaymentResponse
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.RecurringPayment
import hampusborg.bankapp.core.domain.enums.TransactionCategory
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
        val recurringPayment = RecurringPayment(
            userId = request.userId,
            amount = request.amount,
            fromAccountId = request.fromAccountId,
            toAccountId = request.toAccountId,
            interval = request.interval,
            categoryId = TransactionCategory.RECURRING_PAYMENT,
            nextPaymentDate = System.currentTimeMillis(),
            status = "active"
        )
        return mapToResponse(recurringPaymentRepository.save(recurringPayment))
    }

    fun getRecurringPaymentById(id: String): RecurringPaymentResponse {
        val payment = recurringPaymentRepository.findById(id).orElseThrow {
            IllegalArgumentException("Recurring payment not found for ID: $id")
        }
        return mapToResponse(payment)
    }

    fun updateRecurringPayment(paymentId: String, request: RecurringPaymentRequest): RecurringPaymentResponse {
        val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
            IllegalArgumentException("Recurring payment not found")
        }

        recurringPayment.apply {
            amount = request.amount
            interval = request.interval
            toAccountId = request.toAccountId
        }

        return mapToResponse(recurringPaymentRepository.save(recurringPayment))
    }

    fun cancelRecurringPayment(paymentId: String) {
        val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
            IllegalArgumentException("Recurring payment not found")
        }
        recurringPayment.status = "canceled"
        recurringPaymentRepository.save(recurringPayment)
    }

    fun getRecurringPaymentsByUserId(userId: String): List<RecurringPaymentResponse> {
        return recurringPaymentRepository.findByUserId(userId).map { mapToResponse(it) }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun processDueRecurringPayments() {
        val now = System.currentTimeMillis()
        recurringPaymentRepository.findByNextPaymentDateBeforeAndStatus(now, "active")
            .forEach { processPayment(it) }
    }

    private fun processPayment(recurringPayment: RecurringPayment) {
        val transferRequest = createTransferRequest(recurringPayment)

        paymentService.handleTransfer(transferRequest, recurringPayment.userId)
        paymentService.logTransaction(
            fromAccountId = recurringPayment.fromAccountId,
            toAccountId = recurringPayment.toAccountId,
            userId = recurringPayment.userId,
            amount = recurringPayment.amount,
            category = TransactionCategory.RECURRING_PAYMENT
        )

        recurringPayment.nextPaymentDate = calculateNextPaymentDate(recurringPayment.interval)
        recurringPaymentRepository.save(recurringPayment)
    }

    private fun createTransferRequest(recurringPayment: RecurringPayment) = InitiateTransferRequest(
        fromAccountId = recurringPayment.fromAccountId,
        toAccountId = recurringPayment.toAccountId,
        amount = recurringPayment.amount,
        categoryId = TransactionCategory.RECURRING_PAYMENT.name // Pass category as String
    )

    private fun calculateNextPaymentDate(interval: String): Long {
        val calendar = Calendar.getInstance()
        when (interval) {
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            else -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun mapToResponse(payment: RecurringPayment) = RecurringPaymentResponse(
        id = payment.id ?: "",
        userId = payment.userId,
        amount = payment.amount,
        fromAccountId = payment.fromAccountId,
        toAccountId = payment.toAccountId,
        interval = payment.interval,
        status = payment.status,
        categoryId = payment.categoryId.name,
        nextPaymentDate = payment.nextPaymentDate
    )
}