package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.NotEmpty

data class RegisterUserRequest(

    @field:NotEmpty(message = "Username cannot be empty")
    val username: String,

    @field:NotEmpty(message = "Password cannot be empty")
    val password: String,

    @field:NotEmpty(message = "Email cannot be empty")
    val email: String,

    @field:NotEmpty(message = "First name cannot be empty")
    val firstName: String,

    @field:NotEmpty(message = "Lastname cannot be empty")
    val lastName: String

)