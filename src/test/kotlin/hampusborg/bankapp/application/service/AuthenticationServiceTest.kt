import hampusborg.bankapp.application.service.AuthenticationService
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.application.dto.request.RegisterUserRequest
import hampusborg.bankapp.application.dto.request.AuthenticateUserRequest
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder

class AuthenticationServiceTest {

    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val authService: AuthenticationService = AuthenticationService(
        userRepository = userRepository,
        passwordEncoder = mock(PasswordEncoder::class.java),
        jwtUtil = mock(JwtUtil::class.java),
        cacheHelperService = mock(CacheHelperService::class.java)
    )

    @Test
    fun `should throw DuplicateUserException on duplicate registration`() {
        `when`(userRepository.findByUsername("testUser")).thenReturn(User(username = "testUser", password = "testPassword", firstName = "swag", lastName = "testUser", email = "cool@gmail.com"))
        assertThrows(DuplicateUserException::class.java) {
            authService.registerUser(RegisterUserRequest("testUser", "pass", "test@example.com", "Test", "User"))
        }
    }

    @Test
    fun `should authenticate user successfully`() {
        val user = User(username = "testUser", firstName = "lol", lastName = "aja", email = "cool@gmail.com", password = "hashedPassword")
        `when`(userRepository.findByUsername("testUser")).thenReturn(user)
        `when`(mock(PasswordEncoder::class.java).matches(anyString(), anyString())).thenReturn(true)
        authService.loginUser(AuthenticateUserRequest("testUser", "password"))
    }
}