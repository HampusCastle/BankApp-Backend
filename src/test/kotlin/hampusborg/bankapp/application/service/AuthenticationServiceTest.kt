package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.AuthenticateUserRequest
import hampusborg.bankapp.application.dto.request.RegisterUserRequest
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.Role
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestPropertySource(locations = ["classpath:application-test.properties"])
class AuthenticationServiceTest {

    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val jwtUtil: JwtUtil = mock()
    private val rateLimiterService: RateLimiterService = mock() // Mock rate limiter service
    private val cacheHelperService: CacheHelperService = mock() // Mock cache helper service

    private val authenticationService = AuthenticationService(userRepository, passwordEncoder, jwtUtil, rateLimiterService, cacheHelperService)

    @Test
    fun `should throw DuplicateUserException when username already exists`() {
        val registrationRequest = RegisterUserRequest(
            username = "testuser",
            password = "password123",
            email = "testuser@example.com"
        )

        whenever(rateLimiterService.isAllowed("testuser")).thenReturn(true)
        whenever(userRepository.findByUsername(registrationRequest.username)).thenReturn(
            User(username = "testuser", email = "existing@example.com", password = "password123", roles = listOf(Role.USER))
        )

        assertFailsWith<DuplicateUserException> {
            authenticationService.registerUser(registrationRequest)
        }
    }

    @Test
    fun `should register a user successfully`() {
        val registrationRequest = RegisterUserRequest(
            username = "newuser",
            password = "password123",
            email = "newuser@example.com"
        )

        val encodedPassword = "encodedPassword123"
        whenever(passwordEncoder.encode(registrationRequest.password)).thenReturn(encodedPassword)
        whenever(userRepository.findByUsername(registrationRequest.username)).thenReturn(null)
        whenever(userRepository.save(any<User>())).thenAnswer {
            val user = it.getArgument<User>(0)
            user.copy(id = "1")
        }
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)  // Mock rateLimiterService to return true

        val response = authenticationService.registerUser(registrationRequest)

        assertEquals("1", response.id)
        assertEquals("newuser", response.username)
    }

    @Test
    fun `should login a user successfully`() {
        val loginRequest = AuthenticateUserRequest(username = "testuser", password = "password123")

        val user = User(
            id = "1",
            username = "testuser",
            password = "encodedPassword123",
            email = "testuser@example.com",
            roles = listOf(Role.USER)
        )
        val token = "jwtToken123"

        whenever(userRepository.findByUsername(loginRequest.username)).thenReturn(user)
        whenever(passwordEncoder.matches(loginRequest.password, user.password)).thenReturn(true)
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)  // Mock rateLimiterService to return true
        whenever(cacheHelperService.getUserByUsername(loginRequest.username)).thenReturn(user)  // Mock cacheHelperService

        whenever(jwtUtil.generateToken(user.id!!, user.username, user.roles.map { it.name })).thenReturn(token)

        val response = authenticationService.loginUser(loginRequest)

        assertEquals(token, response)
    }

    @Test
    fun `should throw exception when login password is incorrect`() {
        val loginRequest = AuthenticateUserRequest(username = "testuser", password = "wrongpassword")
        val user = User(username = "testuser", password = "encodedPassword123", email = "testuser@example.com", roles = listOf(Role.USER))

        whenever(userRepository.findByUsername(loginRequest.username)).thenReturn(user)
        whenever(passwordEncoder.matches(loginRequest.password, user.password)).thenReturn(false)
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)  // Mock rateLimiterService to return true

        assertFailsWith<RuntimeException> {
            authenticationService.loginUser(loginRequest)
        }
    }
}