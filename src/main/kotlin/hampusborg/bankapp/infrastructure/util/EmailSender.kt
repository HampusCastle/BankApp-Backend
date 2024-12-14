package hampusborg.bankapp.infrastructure.util

import hampusborg.bankapp.application.service.EmailService
import org.springframework.stereotype.Component

@Component
class EmailSender(private val emailService: EmailService) {

    fun sendRegistrationEmail(userId: String, to: String, username: String) {
        val subject = "Welcome to BankApp!"
        val body = "Hello $username,\n\nThank you for registering with BankApp. Your account is now active."
        emailService.sendEmail(userId, to, subject, body) // Make sure to pass userId here
    }

    fun sendPasswordResetEmail(userId: String, to: String, resetLink: String) {
        val subject = "Password Reset Request"
        val body = "To reset your password, please click the following link: $resetLink"
        emailService.sendEmail(userId, to, subject, body) // Make sure to pass userId here
    }
}