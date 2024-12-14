package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.service.base.PasswordResetService
import hampusborg.bankapp.core.domain.User
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PasswordResetServiceTest {

    private val userService: UserService = mock()
    private val mailSender: JavaMailSender = mock()
    private val passwordResetService: PasswordResetService = spy(PasswordResetService(userService, mailSender))

    @Test
    fun `should send password reset email successfully`() {
        val userId = "user1"
        val user = User(id = "user1", username = "username", email = "user@example.com", password = "oldPassword")
        whenever(userService.getUserById(userId)).thenReturn(user)

        passwordResetService.sendPasswordResetEmail(userId)

        val captor = ArgumentCaptor.forClass(SimpleMailMessage::class.java)
        verify(mailSender).send(captor.capture())

        val capturedMessage = captor.value
        assertNotNull(capturedMessage)
        assertEquals("user@example.com", capturedMessage.to?.get(0))
        assertEquals("Password Reset Request", capturedMessage.subject)
    }

    @Test
    fun `should generate reset token correctly`() {
        val user = User(id = "12345", email = "user@example.com", username = "Test User", password = "password")

        val resetToken = passwordResetService.generateResetToken(user)

        assertNotNull(resetToken.token)
        assertEquals(36, resetToken.token.length)
    }

    @Test
    fun `should reset password successfully`() {
        val userId = "12345"
        val user = User(id = userId, email = "user@example.com", username = "Test User", password = "password")

        val newPassword = "newpassword123"

        whenever(userService.getUserById(userId)).thenReturn(user)

        passwordResetService.resetPassword(userId, newPassword)

        verify(userService).updateUser(eq(userId), any())
        assertEquals(newPassword, user.password)
    }
}