package hampusborg.bankapp.application.dto.response

data class RegisteredUserResponse(
    val id: String,
    val username: String,
    val roles: List<String>
)