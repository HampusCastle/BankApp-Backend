package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.service.base.PaymentService
import org.springframework.stereotype.Service

@Service
class ScheduledPaymentProcessor(
    private val scheduledPaymentService: ScheduledPaymentService,
    private val paymentService: PaymentService,
    private val activityLogService: ActivityLogService
) {

    fun processScheduledPayments(currentTime: Long = System.currentTimeMillis()) {
        val payments = scheduledPaymentService.getPaymentsDue(currentTime)

        if (payments.isEmpty()) return

        payments.forEach { payment ->
            try {
                val transaction = paymentService.handleTransfer(
                    InitiateTransferRequest(
                        fromAccountId = payment.fromAccountId,
                        toAccountId = payment.toAccountId,
                        amount = payment.amount,
                        categoryId = payment.categoryId ?: "default-category"
                    ),
                    payment.userId
                )

                activityLogService.logActivity(
                    payment.userId,
                    "Scheduled Payment Processed",
                    "Transaction ID: ${transaction.id}, Amount: ${transaction.amount}"
                )

                payment.nextPaymentDate = calculateNextPaymentDate(payment.schedule, currentTime)
                scheduledPaymentService.save(payment)
            } catch (_: Exception) {
            }
        }
    }

    private fun calculateNextPaymentDate(schedule: String, currentTime: Long): Long {
        return when (schedule) {
            "daily" -> currentTime + 86400000
            "weekly" -> currentTime + 604800000
            "monthly" -> currentTime + 2592000000
            else -> currentTime
        }
    }
}