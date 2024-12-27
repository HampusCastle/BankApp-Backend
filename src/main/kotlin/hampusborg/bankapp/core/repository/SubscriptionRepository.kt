package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.Subscription
import org.springframework.data.mongodb.repository.MongoRepository

interface SubscriptionRepository : MongoRepository<Subscription, String> {
    fun findAllByUserIdAndStatus(userId: String, status: String): List<Subscription>
}