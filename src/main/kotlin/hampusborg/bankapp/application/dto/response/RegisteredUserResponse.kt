package hampusborg.bankapp.application.dto.response

data class RegisteredUserResponse(
    val id: String? = null,
    val username: String,
    val roles: List<String>,
    val message: String? = null
)