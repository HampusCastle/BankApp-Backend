package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val rateLimiterService: RateLimiterService
) {
    fun updateUser(userId: String, updateUserProfileRequest: UpdateUserProfileRequest): User {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        val user = userRepository.findById(userId).orElseThrow {
            UserNotFoundException("User not found with id: $userId")
        }

        user.username = updateUserProfileRequest.username
        user.email = updateUserProfileRequest.email

        updateUserProfileRequest.password.let {
            user.password = passwordEncoder.encode(it)
        }

        return userRepository.save(user)
    }

    @Cacheable(value = ["userCache"], key = "#userId")
    fun getUserById(userId: String): User {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        return userRepository.findById(userId).orElseThrow {
            UserNotFoundException("User not found with id: $userId")
        }
    }
}