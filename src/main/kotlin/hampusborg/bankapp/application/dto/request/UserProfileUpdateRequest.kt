package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserProfileUpdateRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d).*$",
        message = "Password must contain at least one uppercase letter and one number"
    )
    val password: String,

    @field:Email(message = "Email should be valid")
    @field:NotBlank(message = "Email is required")
    val email: String
)