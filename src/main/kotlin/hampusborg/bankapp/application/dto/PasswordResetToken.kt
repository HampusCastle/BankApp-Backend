package hampusborg.bankapp.application.dto

data class PasswordResetToken(
    val token: String,
    val expiration: Long
)