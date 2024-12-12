package hampusborg.bankapp.application.dto.response

data class UserRegistrationResponse(
    val id: String,
    val username: String,
    val roles: List<String>
)