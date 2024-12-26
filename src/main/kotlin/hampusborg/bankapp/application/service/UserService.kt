package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.dto.response.UpdatedUserProfileResponse
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val cacheHelperService: CacheHelperService
) {

    fun updateUserProfile(updateUserProfileRequest: UpdateUserProfileRequest): ResponseEntity<UpdatedUserProfileResponse> {
        val userId = SecurityContextHolder.getContext().authentication.principal as String

        val user = userRepository.findById(userId).orElseThrow {
            UserNotFoundException("User not found with id: $userId")
        }

        user.firstName = updateUserProfileRequest.firstName
        user.lastName = updateUserProfileRequest.lastName
        user.email = updateUserProfileRequest.email
        if (updateUserProfileRequest.password.isNotEmpty()) {
            user.password = updateUserProfileRequest.password
        }

        val updatedUser = userRepository.save(user)

        cacheHelperService.evictCache("userCache", userId)
        cacheHelperService.storeUser(updatedUser)

        val roles = updatedUser.roles.map { it.authority }
        val response = UpdatedUserProfileResponse(
            id = updatedUser.id,
            username = updatedUser.username,
            email = updatedUser.email,
            roles = roles,
            message = "Profile updated successfully"
        )

        return ResponseEntity.ok(response)
    }

    fun getUserProfile(userId: String): ResponseEntity<User> {

        val cachedUser = cacheHelperService.getUserFromCache(userId)
        if (cachedUser != null) {
            return ResponseEntity.ok(cachedUser)
        }

        val user = userRepository.findById(userId).orElseThrow {
            UserNotFoundException("User not found with id: $userId")
        }

        cacheHelperService.storeUser(user)

        return ResponseEntity.ok(user)
    }
    fun getUserById(userId: String): User {
        return userRepository.findById(userId).orElseThrow {
            UserNotFoundException("User not found with id: $userId")
        }
    }
}