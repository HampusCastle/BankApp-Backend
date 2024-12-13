package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.PasswordResetToken
import hampusborg.bankapp.core.domain.User
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.SimpleMailMessage
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PasswordResetServiceTest {

    private val userService: UserService = mock()
    private val mailSender: JavaMailSender = mock()
    private val passwordResetService: PasswordResetService = spy(PasswordResetService(userService, mailSender))

    @Test
    fun `should send password reset email successfully`() {
        val userId = "12345"
        val user = User(id = userId, email = "user@example.com", username = "Test User", password = "password")

        whenever(userService.getUserById(userId)).thenReturn(user)

        val resetToken = "random-token-string"
        val expectedResetLink = "http://yourapp.com/reset-password?token=$resetToken"

        doReturn(PasswordResetToken(resetToken, System.currentTimeMillis() + 3600000))
            .whenever(passwordResetService).generateResetToken(user)

        passwordResetService.sendPasswordResetEmail(userId)

        verify(mailSender).send(argThat<SimpleMailMessage> { message ->
            message.to?.get(0) == user.email &&
                    message.subject == "Password Reset Request" &&
                    message.text?.contains(expectedResetLink) == true
        })
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