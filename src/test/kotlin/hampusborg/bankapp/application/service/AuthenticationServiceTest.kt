package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.application.dto.request.RegisterUserRequest
import hampusborg.bankapp.application.dto.request.AuthenticateUserRequest
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.enums.Role
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder

class AuthenticationServiceTest {

    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val jwtUtil: JwtUtil = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val service = AuthenticationService(userRepository, passwordEncoder, jwtUtil, cacheHelperService)

    @Test
    fun `should throw DuplicateUserException on duplicate registration`() {
        val existingUser = User(
            id = "1",
            username = "testUser",
            password = "hashedPassword",
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            roles = listOf(Role.USER)
        )

        whenever(userRepository.findByUsername("testUser")).thenReturn(existingUser)

        assertThrows<DuplicateUserException> {
            service.registerUser(RegisterUserRequest("testUser", "password", "test@example.com", "Test", "User"))
        }
    }

    @Test
    fun `should authenticate user successfully`() {
        val user = User(
            id = "1",
            username = "testUser",
            password = "hashedPassword",
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            roles = listOf(Role.USER)
        )

        val expectedRoles = user.roles.map { it.authority }

        whenever(userRepository.findByUsername("testUser")).thenReturn(user)
        whenever(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true)
        whenever(jwtUtil.generateToken(eq("1"), eq("testUser"), eq(expectedRoles))).thenReturn("mocked-token")

        val token = service.loginUser(AuthenticateUserRequest("testUser", "password"))

        assertEquals("mocked-token", token)

        verify(jwtUtil, times(1)).generateToken(eq("1"), eq("testUser"), eq(expectedRoles))
    }
}