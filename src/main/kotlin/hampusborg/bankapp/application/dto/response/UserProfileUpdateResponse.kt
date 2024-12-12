package hampusborg.bankapp.application.dto.response

data class UserProfileUpdateResponse(
    val id: String?,
    val username: String?,
    val email: String?,
    val roles: List<String>,
    val message: String
)