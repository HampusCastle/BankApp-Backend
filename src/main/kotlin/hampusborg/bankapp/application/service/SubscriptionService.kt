package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.dto.response.SubscriptionResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Subscription
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.SubscriptionRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentService: PaymentService,
    private val cacheHelperService: CacheHelperService
) {

    fun createSubscription(request: SubscriptionRequest): SubscriptionResponse {

        val userId = SecurityContextHolder.getContext().authentication?.name
            ?: throw ApiRequestException("User is not authenticated")

        val subscription = Subscription(
            userId = userId,
            amount = request.amount,
            serviceName = request.serviceName,
            interval = request.interval,
            categoryId = TransactionCategory.SUBSCRIPTIONS.name,
            nextPaymentDate = System.currentTimeMillis(),
            status = "active",
            fromAccountId = request.fromAccountId ?: "",
            toAccountId = request.toAccountId ?: ""
        )

        val savedSubscription = subscriptionRepository.save(subscription)

        val paymentRequest = InitiateTransferRequest(
            fromAccountId = request.fromAccountId ?: "",
            toAccountId = savedSubscription.toAccountId,
            amount = savedSubscription.amount,
            categoryId = savedSubscription.categoryId
        )

        paymentService.handleSubscriptionPayment(paymentRequest, savedSubscription.userId)

        return mapToResponse(savedSubscription)
    }

    fun getSubscriptionById(id: String): SubscriptionResponse {
        val subscription = cacheHelperService.getSubscriptionById(id)
        return mapToResponse(subscription)
    }

    fun cancelSubscription(id: String) {
        val userId = SecurityContextHolder.getContext().authentication?.name
            ?: throw ApiRequestException("User is not authenticated")

        val subscription = subscriptionRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("Subscription not found")
        }

        if (subscription.userId != userId) {
            throw ApiRequestException("User does not have permission to cancel this subscription")
        }

        subscription.status = "canceled"
        subscriptionRepository.save(subscription)
    }

    fun getSubscriptionsByUserId(userId: String): List<SubscriptionResponse> {
        val subscriptions = cacheHelperService.getSubscriptionsByUserId(userId)
        return subscriptions.map { mapToResponse(it) }
    }

    private fun mapToResponse(subscription: Subscription): SubscriptionResponse {
        return SubscriptionResponse(
            id = subscription.id ?: "",
            userId = subscription.userId,
            amount = subscription.amount,
            serviceName = subscription.serviceName,
            interval = subscription.interval,
            status = subscription.status,
            categoryId = subscription.categoryId ?: "",
            nextPaymentDate = subscription.nextPaymentDate
        )
    }
}