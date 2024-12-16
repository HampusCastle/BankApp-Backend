package hampusborg.bankapp.application.service.base

import hampusborg.bankapp.application.dto.PasswordResetToken
import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.service.UserService
import hampusborg.bankapp.core.domain.User
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import java.util.*

@Service
class PasswordResetService(
    private val userService: UserService,
    private val mailSender: JavaMailSender
) {
    private val tokenExpirationTime = 3600000L // Token expiration time (1 hour)

    fun sendPasswordResetEmail(userId: String) {
        val user = userService.getUserById(userId)
        val resetToken = generateResetToken(user)
        val resetLink = "https://yourapp.com/reset-password?token=${resetToken.token}"

        val message = SimpleMailMessage()
        message.setTo(user.email)
        message.subject = "Password Reset Request"
        message.text = "Click the link below to reset your password: \n$resetLink"

        try {
            mailSender.send(message)
        } catch (e: Exception) {
            throw Exception("Failed to send password reset email", e)
        }
    }

    fun generateResetToken(user: User): PasswordResetToken {
        val token = UUID.randomUUID().toString()
        val expirationTime = System.currentTimeMillis() + tokenExpirationTime
        return PasswordResetToken(token, expirationTime)
    }

    fun isTokenValid(token: String): Boolean {
        val tokenData = retrieveTokenFromStorage(token)
        return tokenData?.let {
            it.expiration > System.currentTimeMillis()
        } == true
    }

    fun resetPassword(userId: String, newPassword: String) {
        val user = userService.getUserById(userId)
        user.password = newPassword // Ensure password is hashed before saving

        val updateRequest = UpdateUserProfileRequest(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            password = newPassword // Ensure password is passed
        )

        try {
            userService.updateUserProfile(updateRequest)
        } catch (e: Exception) {
            throw Exception("Failed to reset password", e)
        }
    }

    private fun retrieveTokenFromStorage(token: String): PasswordResetToken? {
        return null
    }
}