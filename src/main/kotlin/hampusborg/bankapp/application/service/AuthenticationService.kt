package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.AuthenticateUserRequest
import hampusborg.bankapp.application.dto.request.RegisterUserRequest
import hampusborg.bankapp.application.dto.response.RegisteredUserResponse
import hampusborg.bankapp.application.exception.classes.AccountCreationException
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.Role
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val rateLimiterService: RateLimiterService,
    private val cacheHelperService: CacheHelperService
) {

    fun registerUser(registerUserRequest: RegisterUserRequest): RegisteredUserResponse {
        val userId = registerUserRequest.username

        if (!rateLimiterService.isAllowed(userId)) {
            throw RuntimeException("Too many requests, please try again later.")
        }

        userRepository.findByUsername(registerUserRequest.username)?.let {
            throw DuplicateUserException("Username already exists: ${registerUserRequest.username}")
        }

        val encodedPassword = passwordEncoder.encode(registerUserRequest.password)
        val user = User(
            username = registerUserRequest.username,
            password = encodedPassword,
            email = registerUserRequest.email,
            roles = listOf(Role.USER)
        )

        return try {
            val savedUser = userRepository.save(user)
            RegisteredUserResponse(
                id = savedUser.id!!,
                username = savedUser.username,
                roles = savedUser.roles.map { it.name }
            )
        } catch (e: Exception) {
            throw AccountCreationException("Failed to create account for user: ${user.username}")
        }
    }

    fun loginUser(authenticateUserRequest: AuthenticateUserRequest): String {
        val userId = authenticateUserRequest.username

        if (!rateLimiterService.isAllowed(userId)) {
            throw RuntimeException("Too many requests, please try again later.")
        }

        val user = cacheHelperService.getUserByUsername(authenticateUserRequest.username)  // Call cached logic

        if (!passwordEncoder.matches(authenticateUserRequest.password, user.password)) {
            throw RuntimeException("Invalid username or password")
        }

        return jwtUtil.generateToken(
            user.id!!,
            user.username,
            user.roles.map { it.name }
        )
    }
}