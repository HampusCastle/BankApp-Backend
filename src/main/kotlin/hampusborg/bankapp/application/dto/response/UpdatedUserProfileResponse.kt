package hampusborg.bankapp.application.dto.response

data class UpdatedUserProfileResponse(
    val id: String?,
    val username: String?,
    val email: String?,
    val roles: List<String>,
    val message: String
)