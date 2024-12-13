package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.service.NotificationService
import hampusborg.bankapp.core.domain.Notification
import hampusborg.bankapp.infrastructure.util.JwtUtil
import hampusborg.bankapp.presentation.controller.NotificationController
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class NotificationControllerTest {

    private val notificationService: NotificationService = mock(NotificationService::class.java)
    private val jwtUtil: JwtUtil = mock(JwtUtil::class.java)
    private val notificationController = NotificationController(notificationService, jwtUtil)
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(notificationController).build()

    @Test
    fun `should return notifications successfully`() {
        val notifications = listOf(
            Notification(id = "1", message = "New transaction alert", userId = "1", type = "Notification", timestamp = 100),
            Notification(id = "2", message = "Account balance update", userId = "1", type = "Notification", timestamp = 100)
        )

        val token = "validToken"
        `when`(jwtUtil.extractUserDetails(token.substringAfter(" "))).thenReturn(Pair("1", listOf("user")))
        `when`(notificationService.getUserNotifications("1")).thenReturn(notifications)

        val response = mockMvc.perform(get("/notifications")
            .header("Authorization", token))
            .andReturn().response

        assertEquals(HttpStatus.OK.value(), response.status)
        assertTrue(response.contentAsString.contains("New transaction alert"))
        assertTrue(response.contentAsString.contains("Account balance update"))
    }

    @Test
    fun `should return bad request if userId is null from token`() {
        val token = "Bearer invalidToken"
        `when`(jwtUtil.extractUserDetails(token.substringAfter(" "))).thenReturn(null)

        val response = mockMvc.perform(get("/notifications")
            .header("Authorization", token))
            .andReturn().response

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status)
        assertEquals("[]", response.contentAsString)
    }
}