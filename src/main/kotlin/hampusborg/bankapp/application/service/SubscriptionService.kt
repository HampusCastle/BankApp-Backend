package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.dto.response.SubscriptionResponse
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.Subscription
import hampusborg.bankapp.core.repository.SubscriptionRepository
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentService: PaymentService,
    private val rateLimiterService: RateLimiterService,
    private val cacheHelperService: CacheHelperService
) {

    fun createSubscription(request: SubscriptionRequest): SubscriptionResponse {
        if (!rateLimiterService.isAllowed(request.userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        val subscription = Subscription(
            userId = request.userId,
            amount = request.amount,
            serviceName = request.serviceName,
            interval = request.interval,
            categoryId = request.categoryId ?: "",
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
        val subscription = cacheHelperService.getSubscriptionById(id)  // Use CacheHelperService for caching
        return mapToResponse(subscription)
    }

    fun updateSubscription(id: String, request: SubscriptionRequest): SubscriptionResponse {
        if (!rateLimiterService.isAllowed(request.userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        val subscription = subscriptionRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("Subscription not found")
        }

        subscription.amount = request.amount
        subscription.interval = request.interval
        subscription.serviceName = request.serviceName
        subscription.categoryId = request.categoryId ?: subscription.categoryId

        val updatedSubscription = subscriptionRepository.save(subscription)

        val paymentRequest = InitiateTransferRequest(
            fromAccountId = request.fromAccountId ?: "",
            toAccountId = updatedSubscription.toAccountId,
            amount = updatedSubscription.amount,
            categoryId = updatedSubscription.categoryId
        )

        paymentService.handleSubscriptionPayment(paymentRequest, updatedSubscription.userId)

        return mapToResponse(updatedSubscription)
    }

    fun cancelSubscription(id: String) {
        val subscription = subscriptionRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("Subscription not found")
        }

        if (!rateLimiterService.isAllowed(subscription.userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        subscription.status = "canceled"
        subscriptionRepository.save(subscription)
    }

    fun getSubscriptionsByUserId(userId: String): List<SubscriptionResponse> {
        val subscriptions = cacheHelperService.getSubscriptionsByUserId(userId)  // Use CacheHelperService for caching
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