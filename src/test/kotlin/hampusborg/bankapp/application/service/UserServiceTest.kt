package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.dto.response.UpdatedUserProfileResponse
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.argThat
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest {

    private val repository: UserRepository = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val service = UserService(repository, cacheHelperService, passwordEncoder)

    @Test
    fun `should update user profile successfully`() {
        val user = User(
            id = "1",
            username = "testUser",
            password = "hashedPassword",
            firstName = "OldFirst",
            lastName = "OldLast",
            email = "old@example.com"
        )
        val request = UpdateUserProfileRequest(
            firstName = "NewFirst",
            lastName = "NewLast",
            email = "new@example.com",
            password = "newPassword"
        )

        whenever(repository.findById("1")).thenReturn(java.util.Optional.of(user))
        whenever(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword")
        whenever(repository.save(any())).thenReturn(
            user.copy(
                firstName = "NewFirst",
                lastName = "NewLast",
                email = "new@example.com",
                password = "newHashedPassword"
            )
        )

        val response: ResponseEntity<UpdatedUserProfileResponse> = service.updateUserProfile(request)

        assertEquals("new@example.com", response.body!!.email)
        assertEquals("Profile updated successfully", response.body!!.message)

        verify(repository, times(1)).save(argThat {
            assertEquals("NewFirst", this.firstName)
            assertEquals("NewLast", this.lastName)
            assertEquals("new@example.com", this.email)
            true
        })
    }
}
