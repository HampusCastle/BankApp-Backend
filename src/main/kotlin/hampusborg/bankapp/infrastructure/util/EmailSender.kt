package hampusborg.bankapp.infrastructure.util

import hampusborg.bankapp.application.service.EmailService
import org.springframework.stereotype.Component

@Component
class EmailSender(private val emailService: EmailService) {

    fun sendRegistrationEmail(to: String, username: String) {
        val subject = "Welcome to BankApp!"
        val body = "Hello $username,\n\nThank you for registering with BankApp. Your account is now active."
        emailService.sendEmail(to, subject, body)
    }

    fun sendPasswordResetEmail(to: String, resetLink: String) {
        val subject = "Password Reset Request"
        val body = "To reset your password, please click the following link: $resetLink"
        emailService.sendEmail(to, subject, body)
    }
}