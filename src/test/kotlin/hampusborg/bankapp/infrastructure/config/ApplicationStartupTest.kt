package hampusborg.bankapp.infrastructure.config

import hampusborg.bankapp.core.domain.Role
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder

class ApplicationStartupTest {

    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private lateinit var applicationStartup: ApplicationStartup

    @BeforeEach
    fun setUp() {
        applicationStartup = ApplicationStartup(userRepository, passwordEncoder)
    }

    @Test
    fun `seedDatabase should populate database when empty`() {
        whenever(userRepository.count()).thenReturn(0L)
        whenever(passwordEncoder.encode("admin123")).thenReturn("encodedAdmin123")
        (1..9).forEach { index ->
            whenever(passwordEncoder.encode("user$index")).thenReturn("encodedUser$index")
        }

        applicationStartup.seedDatabase()

        verify(userRepository).save(
            check { user ->
                assert(user.username == "admin")
                assert(user.password == "encodedAdmin123")
                assert(user.roles == listOf(Role.ADMIN))
            }
        )
        verify(userRepository).saveAll(
            check<List<User>> { users ->
                assert(users.size == 9)
                users.forEachIndexed { index, user ->
                    assert(user.username == "user${index + 1}")
                    assert(user.password == "encodedUser${index + 1}")
                    assert(user.roles == listOf(Role.USER))
                }
            }
        )
    }

    @Test
    fun `seedDatabase should not modify database when not empty`() {
        whenever(userRepository.count()).thenReturn(1L)

        applicationStartup.seedDatabase()

        verify(userRepository, never()).save(any<User>())
        verify(userRepository, never()).saveAll(any<List<User>>())
    }

    @Test
    fun `seedDatabase should assign correct roles to users`() {
        whenever(userRepository.count()).thenReturn(0L)
        whenever(passwordEncoder.encode(any())).thenReturn("mockedPassword")

        applicationStartup.seedDatabase()

        verify(userRepository).save(
            check { user ->
                assert(user.username == "admin")
                assert(user.roles.contains(Role.ADMIN))
            }
        )
        verify(userRepository).saveAll(
            check<List<User>> { users ->
                assert(users.all { it.roles.contains(Role.USER) })
            }
        )
    }
}
