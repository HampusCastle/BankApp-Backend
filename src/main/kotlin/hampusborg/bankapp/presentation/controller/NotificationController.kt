package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.NotificationRequest
import hampusborg.bankapp.application.dto.response.NotificationResponse
import hampusborg.bankapp.application.service.NotificationService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val jwtUtil: JwtUtil
) {

    @GetMapping
    fun getNotifications(@RequestHeader("Authorization") token: String): ResponseEntity<List<NotificationResponse>> {
        val userId = extractUserIdFromToken(token)
        return if (userId != null) {
            val notifications = notificationService.getUserNotifications(userId).map { notification ->
                NotificationResponse(
                    id = notification.id,
                    userId = notification.userId,
                    message = notification.message,
                    timestamp = notification.timestamp,
                    type = notification.type
                )
            }
            ResponseEntity.ok(notifications)
        } else {
            ResponseEntity.badRequest().body(emptyList())
        }
    }

    @PostMapping
    fun createNotification(
        @RequestBody notificationRequest: NotificationRequest
    ): ResponseEntity<NotificationResponse> {
        return try {
            val notification = notificationService.createNotification(notificationRequest)
            val notificationResponse = NotificationResponse(
                id = notification.id,
                userId = notification.userId,
                message = notification.message,
                timestamp = notification.timestamp,
                type = notification.type
            )
            ResponseEntity.ok(notificationResponse)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }
    }

    private fun extractUserIdFromToken(token: String): String? {
        return jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
    }
}