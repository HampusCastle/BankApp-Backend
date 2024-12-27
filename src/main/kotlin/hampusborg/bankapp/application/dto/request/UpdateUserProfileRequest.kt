package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UpdateUserProfileRequest(
    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)