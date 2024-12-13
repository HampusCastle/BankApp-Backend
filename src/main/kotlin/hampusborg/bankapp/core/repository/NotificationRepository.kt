package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.Notification
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : MongoRepository<Notification, String> {
    fun save(notification: Notification): Notification
    fun findByUserId(userId: String): List<Notification>
}