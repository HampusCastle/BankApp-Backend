package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.NotEmpty

data class AuthenticateUserRequest(
    @field:NotEmpty(message = "Username cannot be empty")
    val username: String,

    @field:NotEmpty(message = "Password cannot be empty")
    val password: String
)