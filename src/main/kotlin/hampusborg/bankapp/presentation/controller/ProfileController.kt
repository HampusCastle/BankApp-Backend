package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.dto.response.UpdatedUserProfileResponse
import hampusborg.bankapp.application.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/profile")
class ProfileController(
    private val userService: UserService
) {
    @PutMapping
    fun updateUserProfile(
        @Valid @RequestBody updateUserProfileRequest: UpdateUserProfileRequest
    ): ResponseEntity<UpdatedUserProfileResponse> {
        val userId = SecurityContextHolder.getContext().authentication?.principal as? String
        if (userId == null) {
            return ResponseEntity.badRequest().body(
                UpdatedUserProfileResponse(
                    id = null,
                    username = null,
                    email = null,
                    roles = emptyList(),
                    message = "User not found"
                )
            )
        }

        return try {
            val updatedUser = userService.updateUser(userId, updateUserProfileRequest)

            val roles = updatedUser.roles.map { it.authority }
            val response = UpdatedUserProfileResponse(
                id = updatedUser.id,
                username = updatedUser.username,
                email = updatedUser.email,
                roles = roles,
                message = "Profile updated successfully"
            )

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            val errorResponse = UpdatedUserProfileResponse(
                id = null,
                username = null,
                email = null,
                roles = emptyList(),
                message = e.message ?: "Unknown error"
            )
            ResponseEntity.badRequest().body(errorResponse)
        }
    }
}