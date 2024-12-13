package hampusborg.bankapp.application.service


import hampusborg.bankapp.application.dto.request.UserProfileUpdateRequest
import hampusborg.bankapp.application.exception.classes.UserNotFoundException
import hampusborg.bankapp.core.domain.Role
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class UserServiceTest {

    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val userService = UserService(userRepository, passwordEncoder)

    @Test
    fun `should update user profile successfully`() {
        val userId = "12345"
        val existingUser = User(
            id = userId,
            username = "oldusername",
            email = "oldemail@example.com",
            password = "oldpassword",
            roles = listOf(Role.USER)
        )

        val updateRequest = UserProfileUpdateRequest(
            username = "newusername",
            email = "newemail@example.com",
            password = "newpassword123"
        )

        val encodedPassword = "encodedpassword"

        whenever(userRepository.findById(userId)).thenReturn(java.util.Optional.of(existingUser))
        whenever(passwordEncoder.encode(updateRequest.password)).thenReturn(encodedPassword)
        whenever(userRepository.save(any<User>())).thenAnswer { invocation ->
            val user = invocation.arguments[0] as User
            user.copy(id = userId)
        }

        val result = userService.updateUser(userId, updateRequest)

        assertEquals("newusername", result.username)
        assertEquals("newemail@example.com", result.email)
        assertEquals(encodedPassword, result.password)

        verify(userRepository).save(check {
            assertEquals("newusername", it.username)
            assertEquals("newemail@example.com", it.email)
            assertEquals(encodedPassword, it.password)
        })
    }


    @Test
    fun `should throw UserNotFoundException when user not found`() {
        val userId = "12345"
        val updateRequest = UserProfileUpdateRequest(
            username = "newusername",
            email = "newemail@example.com",
            password = "newpassword123"
        )

        whenever(userRepository.findById(userId)).thenReturn(java.util.Optional.empty())

        val exception = assertFailsWith<UserNotFoundException> {
            userService.updateUser(userId, updateRequest)
        }

        assertEquals("User not found with id: $userId", exception.message)
    }


    @Test
    fun `should get user by ID successfully`() {
        val userId = "12345"
        val user = User(
            id = userId,
            username = "testuser",
            email = "testuser@example.com",
            password = "password123",
            roles = listOf(Role.USER)
        )

        whenever(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user))

        val result = userService.getUserById(userId)

        assertEquals("testuser", result.username)
        assertEquals("testuser@example.com", result.email)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `should throw UserNotFoundException when user not found by ID`() {
        val userId = "12345"
        whenever(userRepository.findById(userId)).thenReturn(java.util.Optional.empty())

        assertFailsWith<UserNotFoundException> {
            userService.getUserById(userId)
        }
    }
}