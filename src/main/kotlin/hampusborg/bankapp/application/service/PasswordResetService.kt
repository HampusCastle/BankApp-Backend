package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.PasswordResetToken
import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
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
    private val tokenExpirationTime = 3600000L

    fun sendPasswordResetEmail(userId: String) {
        val user = userService.getUserById(userId)
        val resetToken = generateResetToken(user)
        val resetLink = "http://yourapp.com/reset-password?token=${resetToken.token}"

        val message = SimpleMailMessage()
        message.setTo(user.email)
        message.setSubject("Password Reset Request")
        message.setText("Click the link below to reset your password: \n$resetLink")

        mailSender.send(message)
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
        user.password = newPassword

        val updateRequest = UpdateUserProfileRequest(
            username = user.username,
            email = user.email,
            password = newPassword
        )

        userService.updateUser(userId, updateRequest)
    }

    private fun retrieveTokenFromStorage(token: String): PasswordResetToken? {
        return null
    }
}