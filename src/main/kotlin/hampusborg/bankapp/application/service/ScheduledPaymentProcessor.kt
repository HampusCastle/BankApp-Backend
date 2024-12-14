package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.core.repository.TransactionCategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ScheduledPaymentProcessor(
    private val scheduledPaymentService: ScheduledPaymentService,
    private val transferService: TransferService,
    private val activityLogService: ActivityLogService,
    private val transactionCategoryRepository: TransactionCategoryRepository
) {
    fun processScheduledPayments(currentTime: Long = System.currentTimeMillis()) {

        val log = LoggerFactory.getLogger(ScheduledPaymentProcessor::class.java)

        val payments = scheduledPaymentService.getPaymentsDue(currentTime)
        if (payments.isEmpty()) {
            log.warn("No payments to process at this time.")
            return
        }

        payments.forEach { payment ->
            try {
                val categoryId = payment.categoryId ?: transactionCategoryRepository.findByName("Default")?.id
                ?: "default-category-id"

                val initiateTransferRequest = InitiateTransferRequest(
                    fromAccountId = payment.fromAccountId,
                    toAccountId = payment.toAccountId,
                    amount = payment.amount,
                    categoryId = categoryId
                )
                transferService.transferFunds(initiateTransferRequest, payment.userId)

                activityLogService.logActivity(
                    payment.userId,
                    "Scheduled payment processed",
                    "Transferred ${payment.amount} from ${payment.fromAccountId} to ${payment.toAccountId}."
                )
                payment.nextPaymentDate = calculateNextPaymentDate(payment.schedule, currentTime)
                scheduledPaymentService.save(payment)
            } catch (e: Exception) {
                log.error("Failed to process payment ${payment.id}: ${e.message}")
            }
        }
    }

    private fun calculateNextPaymentDate(schedule: String, currentDate: Long): Long =
        when (schedule) {
            "daily" -> currentDate + 86400000
            "weekly" -> currentDate + 604800000
            "monthly" -> currentDate + 2592000000
            else -> currentDate
        }
}
