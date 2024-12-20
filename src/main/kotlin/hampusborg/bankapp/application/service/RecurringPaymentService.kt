package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.dto.response.RecurringPaymentResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.RecurringPayment
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.RecurringPaymentRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

@Service
class RecurringPaymentService(
    private val recurringPaymentRepository: RecurringPaymentRepository,
    private val paymentService: PaymentService
) {

    private val logger = LoggerFactory.getLogger(RecurringPaymentService::class.java)

    fun createRecurringPayment(request: RecurringPaymentRequest): RecurringPaymentResponse {
        try {
            val userId = SecurityContextHolder.getContext().authentication?.name
                ?: throw ApiRequestException("User is not authenticated")

            logger.info("Creating recurring payment for user: $userId with request: $request")

            val recurringPayment = RecurringPayment(
                userId = userId,
                amount = request.amount,
                fromAccountId = request.fromAccountId,
                toAccountId = request.toAccountId,
                interval = request.interval,
                categoryId = TransactionCategory.RECURRING_PAYMENT,
                status = "active",
                nextPaymentDate = calculateNextPaymentDate(request.interval)
            )

            val savedPayment = recurringPaymentRepository.save(recurringPayment)
            logger.info("Recurring payment created successfully with ID: ${savedPayment.id}")

            return RecurringPaymentResponse(
                id = savedPayment.id!!,
                userId = savedPayment.userId,
                amount = savedPayment.amount,
                fromAccountId = savedPayment.fromAccountId,
                toAccountId = savedPayment.toAccountId,
                interval = savedPayment.interval,
                status = savedPayment.status,
                categoryId = savedPayment.categoryId.name,
                nextPaymentDate = savedPayment.nextPaymentDate
            )
        } catch (e: Exception) {
            logger.error("Unexpected error creating recurring payment: ${e.message}", e)
            throw ApiRequestException("An unexpected error occurred while creating the recurring payment: ${e.message}")
        }
    }



    fun updateRecurringPayment(paymentId: String, request: RecurringPaymentRequest): RecurringPaymentResponse {
        try {
            val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
                logger.error("Recurring payment with ID $paymentId not found")
                IllegalArgumentException("Recurring payment not found")
            }
            logger.info("Updating recurring payment ID: $paymentId with new values - amount: ${request.amount}, interval: ${request.interval}, toAccountId: ${request.toAccountId}")

            recurringPayment.apply {
                amount = request.amount
                interval = request.interval
                toAccountId = request.toAccountId
            }

            return mapToResponse(recurringPaymentRepository.save(recurringPayment))

        } catch (e: Exception) {
            logger.error("Error updating recurring payment: ${e.message}", e)
            throw ApiRequestException("Failed to update recurring payment: ${e.message}")
        }
    }

    fun cancelRecurringPayment(paymentId: String) {
        try {
            val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
                logger.error("Recurring payment with ID $paymentId not found for cancellation")
                IllegalArgumentException("Recurring payment not found")
            }

            logger.info("Canceling recurring payment with ID: $paymentId")

            recurringPayment.status = "canceled"
            recurringPaymentRepository.save(recurringPayment)

        } catch (e: Exception) {
            logger.error("Error canceling recurring payment: ${e.message}", e)
            throw ApiRequestException("Failed to cancel recurring payment: ${e.message}")
        }
    }

    fun getRecurringPaymentsByUserId(userId: String): List<RecurringPaymentResponse> {
        return try {
            recurringPaymentRepository.findByUserId(userId).map { mapToResponse(it) }
        } catch (e: Exception) {
            logger.error("Error fetching recurring payments for userId: $userId, ${e.message}", e)
            throw ApiRequestException("Failed to fetch recurring payments for user: $userId")
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun processDueRecurringPayments() {
        try {
            val now = System.currentTimeMillis()
            recurringPaymentRepository.findByNextPaymentDateBeforeAndStatus(now, "active")
                .forEach { processPayment(it) }
        } catch (e: Exception) {
            logger.error("Error processing due recurring payments: ${e.message}", e)
            throw ApiRequestException("Failed to process due recurring payments")
        }
    }

    private fun processPayment(recurringPayment: RecurringPayment) {
        try {
            val transferRequest = createTransferRequest(recurringPayment)

            logger.info("Processing payment for recurring payment ID: ${recurringPayment.id}")

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

        } catch (e: Exception) {
            logger.error("Error processing recurring payment ID: ${recurringPayment.id}, ${e.message}", e)
            throw ApiRequestException("Failed to process recurring payment ID: ${recurringPayment.id}")
        }
    }

    private fun createTransferRequest(recurringPayment: RecurringPayment) = InitiateTransferRequest(
        fromAccountId = recurringPayment.fromAccountId,
        toAccountId = recurringPayment.toAccountId,
        amount = recurringPayment.amount,
        categoryId = TransactionCategory.RECURRING_PAYMENT.name
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