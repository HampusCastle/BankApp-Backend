package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.RecurringPaymentRequest
import hampusborg.bankapp.application.dto.response.RecurringPaymentResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.CacheHelperService
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
    private val paymentService: PaymentService,
    private val cacheHelperService: CacheHelperService
) {

    private val log = LoggerFactory.getLogger(RecurringPaymentService::class.java)

    fun createRecurringPayment(request: RecurringPaymentRequest): RecurringPaymentResponse {
        val userId = SecurityContextHolder.getContext().authentication?.name
            ?: throw ApiRequestException("User is not authenticated")

        log.info("Creating recurring payment for user: $userId")

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
        log.info("Saved recurring payment: $savedPayment")

        return mapToResponse(savedPayment)
    }

    fun updateRecurringPayment(paymentId: String, request: RecurringPaymentRequest): RecurringPaymentResponse {
        val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
            ApiRequestException("Recurring payment not found")
        }

        recurringPayment.apply {
            amount = request.amount
            interval = request.interval
            toAccountId = request.toAccountId
        }

        val updatedPayment = recurringPaymentRepository.save(recurringPayment)
        cacheHelperService.storeRecurringPayment(updatedPayment.id!!, updatedPayment)
        cacheHelperService.evictMonthlyExpensesCache(updatedPayment.userId)

        return mapToResponse(updatedPayment)
    }

    fun cancelRecurringPayment(paymentId: String) {
        val recurringPayment = recurringPaymentRepository.findById(paymentId).orElseThrow {
            ApiRequestException("Recurring payment not found")
        }

        recurringPayment.status = "canceled"
        recurringPaymentRepository.save(recurringPayment)
        cacheHelperService.evictCache("recurringPayments", recurringPayment.userId)
        cacheHelperService.evictMonthlyExpensesCache(recurringPayment.userId)
    }

    fun getAllRecurringPayments(userId: String): List<RecurringPaymentResponse> {
        log.info("Fetching recurring payments for user {} from cache.", userId)
        val cachedPayments = cacheHelperService.getCache("recurringPayments", userId, List::class.java)
            ?.filterIsInstance<RecurringPayment>()

        if (!cachedPayments.isNullOrEmpty()) {
            log.info("Returning cached recurring payments for user {}: {}", userId, cachedPayments)
            return cachedPayments.map { mapToResponse(it) }
        }

        log.warn("Cache miss for recurring payments. Fetching from database.")
        val payments = recurringPaymentRepository.findByUserId(userId)
        if (payments.isNotEmpty()) {
            log.info("Storing payments in cache for user {}: {}", userId, payments)
            cacheHelperService.storeRecurringPaymentsForUser(userId, payments)
        }
        return payments.map { mapToResponse(it) }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun processDueRecurringPayments() {
        val now = System.currentTimeMillis()
        val duePayments = recurringPaymentRepository.findByNextPaymentDateBeforeAndStatus(now, "active")

        val affectedUsers = mutableSetOf<String>()
        duePayments.forEach { payment ->
            processPayment(payment)
            affectedUsers.add(payment.userId)
        }

        affectedUsers.forEach { userId ->
            cacheHelperService.evictMonthlyExpensesCache(userId)
        }
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

        cacheHelperService.handleAccountCacheUpdate(recurringPayment.userId)

        recurringPayment.nextPaymentDate = calculateNextPaymentDate(recurringPayment.interval)
        recurringPaymentRepository.save(recurringPayment)
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