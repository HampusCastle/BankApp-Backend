import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.service.UserService
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest {

    private val repository: UserRepository = mock(UserRepository::class.java)
    private val service: UserService = UserService(
        userRepository = repository,
        cacheHelperService = mock(CacheHelperService::class.java),
        passwordEncoder = mock(PasswordEncoder::class.java)
    )

    @Test
    fun `should fetch user profile`() {
        `when`(repository.findById("testUser")).thenReturn(java.util.Optional.of(mock(User::class.java)))
        service.getUserProfile("testUser")
        verify(repository, times(1)).findById("testUser")
    }

    @Test
    fun `should update user profile`() {
        `when`(repository.findById("testUser")).thenReturn(java.util.Optional.of(mock(User::class.java)))
        service.updateUserProfile(mock(UpdateUserProfileRequest::class.java))
        verify(repository, times(1)).save(any(User::class.java))
    }
}