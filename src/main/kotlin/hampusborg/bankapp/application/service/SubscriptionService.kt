package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.dto.response.SubscriptionResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.core.domain.Subscription
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.SubscriptionRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository
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
        return savedSubscription.toResponse()
    }

    fun getSubscriptionsByStatus(userId: String, status: String): List<SubscriptionResponse> {
        val subscriptions = subscriptionRepository.findAllByUserIdAndStatus(userId, status)
        return subscriptions.map { it.toResponse() }
    }
    fun cancelSubscription(id: String) {
        val subscription = subscriptionRepository.findById(id).orElseThrow {
            IllegalArgumentException("Subscription not found")
        }
        subscription.status = "canceled"
        subscriptionRepository.save(subscription)
    }

    private fun Subscription.toResponse(): SubscriptionResponse {
        return SubscriptionResponse(
            id = this.id ?: "",
            userId = this.userId,
            amount = this.amount,
            serviceName = this.serviceName,
            interval = this.interval,
            status = this.status,
            categoryId = this.categoryId ?: "",
            nextPaymentDate = this.nextPaymentDate
        )
    }
}