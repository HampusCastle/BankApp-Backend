package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.UserProfileUpdateRequest
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun updateUser(userId: String, userProfileUpdateRequest: UserProfileUpdateRequest): User {
        val user = userRepository.findById(userId).orElseThrow {
            UserNotFoundException("User not found with id: $userId")
        }

        user.username = userProfileUpdateRequest.username
        user.email = userProfileUpdateRequest.email

        userProfileUpdateRequest.password.let {
            user.password = passwordEncoder.encode(it)
        }

        return userRepository.save(user)
    }

    fun getUserById(userId: String): User {
        logger.info("Fetching user by id: $userId")
        return userRepository.findById(userId).orElseThrow {
            throw UserNotFoundException("User not found with id: $userId")
        }
    }
}