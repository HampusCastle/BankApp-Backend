package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.application.exception.classes.NotificationCreationException
import hampusborg.bankapp.core.domain.Notification
import hampusborg.bankapp.core.repository.NotificationRepository
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    fun createNotification(request: SendNotificationRequest): Notification {
        return try {
            val notification = Notification(
                userId = request.userId,
                message = request.message,
                timestamp = System.currentTimeMillis(),
                type = request.type
            )
            notificationRepository.save(notification)
        } catch (e: Exception) {
            throw NotificationCreationException("Failed to create notification: ${e.message}")
        }
    }

    fun getUserNotifications(userId: String): List<Notification> {
        return notificationRepository.findByUserId(userId)
    }
}
