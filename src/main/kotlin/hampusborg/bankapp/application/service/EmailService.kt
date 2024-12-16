package hampusborg.bankapp.application.service

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,
) {

    fun sendEmail(to: String, subject: String, body: String, userId: String) {


        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(body, true)
        mailSender.send(message)
    }
}