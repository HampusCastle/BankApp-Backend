package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.application.exception.classes.NotificationCreationException
import hampusborg.bankapp.core.domain.Notification
import hampusborg.bankapp.core.repository.NotificationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NotificationServiceTest {

    private val notificationRepository: NotificationRepository = mock()
    private val notificationService = NotificationService(notificationRepository)

    @Test
    fun `should create a notification successfully`() {
        val userId = "12345"
        val message = "Your payment was successful"
        val type = "payment_success"

        val sendNotificationRequest = SendNotificationRequest(
            userId = userId,
            message = message,
            type = type
        )

        val notification = Notification(
            id = "generated-id-123",
            userId = userId,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type
        )

        whenever(notificationRepository.save(any())).thenAnswer { invocation ->
            val arg = invocation.getArgument<Notification>(0)
            arg.copy(id = notification.id)
        }

        val result = notificationService.createNotification(sendNotificationRequest)

        assertTrue(result.id != null)
        assertEquals("generated-id-123", result.id)
        assertEquals(message, result.message)
        assertEquals(type, result.type)
        assertEquals(userId, result.userId)
        verify(notificationRepository).save(any())
    }

    @Test
    fun `should throw exception when notification creation fails`() {
        val userId = "12345"
        val message = "Test message"
        val type = "test_type"

        val sendNotificationRequest = SendNotificationRequest(
            userId = userId,
            message = message,
            type = type
        )

        whenever(notificationRepository.save(any<Notification>())).thenThrow(RuntimeException("DB error"))

        val exception = assertFailsWith<NotificationCreationException> {
            notificationService.createNotification(sendNotificationRequest)
        }

        assertEquals("Failed to create notification: DB error", exception.message)
    }

    @Test
    fun `should return user notifications`() {
        val userId = "12345"
        val notification1 = Notification(
            id = "1",
            userId = userId,
            message = "Payment received",
            timestamp = System.currentTimeMillis(),
            type = "payment"
        )
        val notification2 = Notification(
            id = "2",
            userId = userId,
            message = "Account updated",
            timestamp = System.currentTimeMillis(),
            type = "account"
        )

        whenever(notificationRepository.findByUserId(userId)).thenReturn(listOf(notification1, notification2))

        val result = notificationService.getUserNotifications(userId)

        assertEquals(2, result.size)
        assertTrue(result.contains(notification1))
        assertTrue(result.contains(notification2))
    }
}
