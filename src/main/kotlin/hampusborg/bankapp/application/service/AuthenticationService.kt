package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.UserLoginRequest
import hampusborg.bankapp.application.dto.request.UserRegistrationRequest
import hampusborg.bankapp.application.dto.response.UserRegistrationResponse
import hampusborg.bankapp.application.exception.classes.AccountCreationException
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.core.domain.Role
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {
    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    fun registerUser(userRegistrationRequest: UserRegistrationRequest): UserRegistrationResponse {
        logger.info("Attempting to register user: ${userRegistrationRequest.username}")

        userRepository.findByUsername(userRegistrationRequest.username)?.let {
            logger.error("Username already exists: ${userRegistrationRequest.username}")
            throw DuplicateUserException("Username already exists: ${userRegistrationRequest.username}")
        }

        val encodedPassword = passwordEncoder.encode(userRegistrationRequest.password)
        val user = User(
            username = userRegistrationRequest.username,
            password = encodedPassword,
            email = userRegistrationRequest.email,
            roles = listOf(Role.USER)
        )

        return try {
            logger.info("User registered successfully: ${user.username}")
            val savedUser = userRepository.save(user)
            UserRegistrationResponse(
                id = savedUser.id!!,
                username = savedUser.username,
                roles = savedUser.roles.map { it.name }
            )
        } catch (e: Exception) {
            logger.error("Failed to create user: ${user.username}, error: ${e.message}")
            throw AccountCreationException("Failed to create account for user: ${user.username}")
        }
    }

    fun loginUser(userLoginRequest: UserLoginRequest): String {
        logger.info("Attempting to authenticate user: ${userLoginRequest.username}")

        val user = userRepository.findByUsername(userLoginRequest.username)
            ?: throw RuntimeException("Invalid username or password")

        if (!passwordEncoder.matches(userLoginRequest.password, user.password)) {
            throw RuntimeException("Invalid username or password")
        }

        logger.info("Authentication successful for user: ${user.username}")
        return jwtUtil.generateToken(
            user.id!!,
            user.username,
            user.roles.map { it.name }
        )
    }
}