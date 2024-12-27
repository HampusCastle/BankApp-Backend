import hampusborg.bankapp.application.dto.request.SendNotificationRequest
import hampusborg.bankapp.application.service.NotificationService
import hampusborg.bankapp.core.domain.Notification
import hampusborg.bankapp.core.repository.NotificationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class NotificationServiceTest {

    private val notificationRepository: NotificationRepository = mock(NotificationRepository::class.java)
    private val notificationService: NotificationService = NotificationService(notificationRepository)

    @Test
    fun `should fetch notifications for user`() {
        `when`(notificationRepository.findByUserId("testUser")).thenReturn(emptyList())
        val notifications = notificationService.getUserNotifications("testUser")
        assertEquals(0, notifications.size)
    }

    @Test
    fun `should create notification successfully`() {
        val notification = mock(Notification::class.java)
        `when`(notificationRepository.save(any(Notification::class.java))).thenReturn(notification)
        notificationService.createNotification(SendNotificationRequest("testUser", "Test Message", "INFO"))
        verify(notificationRepository, times(1)).save(any(Notification::class.java))
    }
}