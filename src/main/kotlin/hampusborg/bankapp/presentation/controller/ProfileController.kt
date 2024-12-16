package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.service.UserService
import hampusborg.bankapp.application.dto.response.UpdatedUserProfileResponse
import hampusborg.bankapp.core.domain.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class ProfileController(
    private val userService: UserService
) {

    @GetMapping("/profile")
    fun getUserProfile(): ResponseEntity<User> {
        val userId = SecurityContextHolder.getContext().authentication.principal as String
        return userService.getUserProfile(userId)
    }

    @PutMapping("/profile")
    fun updateUserProfile(
        @RequestBody updateUserProfileRequest: UpdateUserProfileRequest
    ): ResponseEntity<UpdatedUserProfileResponse> {
        return userService.updateUserProfile(updateUserProfileRequest)
    }
}