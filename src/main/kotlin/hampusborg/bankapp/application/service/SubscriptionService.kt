package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.dto.response.SubscriptionResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Subscription
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.SubscriptionRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
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
            categoryId = request.categoryId ?: TransactionCategory.SUBSCRIPTIONS.name,
            nextPaymentDate = System.currentTimeMillis(),
            status = "active",
            fromAccountId = request.fromAccountId ?: "",
            toAccountId = request.toAccountId ?: ""
        )

        val savedSubscription = subscriptionRepository.save(subscription)

        cacheHelperService.evictCache("subscriptions", userId)
        cacheHelperService.storeSubscription(savedSubscription.id!!, savedSubscription)
        cacheHelperService.refreshSubscriptionsCache(userId, "active")

        return savedSubscription.toResponse()
    }

    fun getSubscriptionsByStatus(userId: String, status: String): List<SubscriptionResponse> {
        val cachedSubscriptions = cacheHelperService.getSubscriptionsByUserAndStatus(userId, status)
        if (!cachedSubscriptions.isNullOrEmpty()) {
            return cachedSubscriptions.map { it.toResponse() }
        }

        val subscriptions = subscriptionRepository.findAllByUserIdAndStatus(userId, status)

        cacheHelperService.storeSubscriptionsByUserAndStatus(userId, status, subscriptions)

        return subscriptions.map { it.toResponse() }
    }

    fun cancelSubscription(id: String) {
        val subscription = subscriptionRepository.findById(id).orElseThrow {
            IllegalArgumentException("Subscription not found")
        }

        subscription.status = "canceled"

        val updatedSubscription = subscriptionRepository.save(subscription)

        cacheHelperService.evictSubscriptionsCache(subscription.userId, subscription.status)
        cacheHelperService.storeSubscription(updatedSubscription.id!!, updatedSubscription)
    }

    private fun Subscription.toResponse(): SubscriptionResponse {
        return SubscriptionResponse(
            id = this.id ?: "",
            userId = this.userId,
            amount = this.amount,
            serviceName = this.serviceName,
            interval = this.interval,
            status = this.status,
            categoryId = this.categoryId,
            nextPaymentDate = this.nextPaymentDate
        )
    }
}