package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.UpdateUserProfileRequest
import hampusborg.bankapp.application.service.UserService
import hampusborg.bankapp.core.domain.Role
import hampusborg.bankapp.core.domain.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.mockito.MockitoAnnotations

class ProfileControllerTest {

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var profileController: ProfileController

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(profileController).build()
    }

    @Test
    fun `should update user profile successfully`() {
        val userId = "user123"
        val authentication = UsernamePasswordAuthenticationToken(userId, null)
        SecurityContextHolder.setContext(SecurityContextImpl(authentication))

        val request = UpdateUserProfileRequest(username = "newusername", email = "newemail@example.com", password = "Newpassword123")
        val updatedUser = User(id = "1", email = "newemail@example.com", username = "newusername", password = "Newpassword123", roles = listOf(Role.USER))

        `when`(userService.updateUser(userId, request)).thenReturn(updatedUser)

        mockMvc.perform(
            put("/profile")
                .contentType("application/json")
                .content("""{"username":"newusername","email":"newemail@example.com","password":"Newpassword123"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().json("""{"message":"Profile updated successfully","id":"1","username":"newusername","email":"newemail@example.com","roles":["ROLE_USER"]}"""))
    }

    @Test
    fun `should return bad request when user is not authenticated`() {
        SecurityContextHolder.clearContext()

        mockMvc.perform(
            put("/profile")
                .contentType("application/json")
                .content("""{"username":"newusername","email":"newemail@example.com","password":"Newpassword123"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().json("""{"message":"User not found","id":null,"username":null,"email":null,"roles":[]}"""))
    }

    @Test
    fun `should return bad request when user profile update fails`() {
        val userId = "user123"
        val authentication = UsernamePasswordAuthenticationToken(userId, null)
        SecurityContextHolder.setContext(SecurityContextImpl(authentication))

        val request = UpdateUserProfileRequest(username = "newusername", email = "newemail@example.com", password = "Newpassword123")
        `when`(userService.updateUser(userId, request)).thenThrow(RuntimeException("Failed to update"))

        mockMvc.perform(
            put("/profile")
                .contentType("application/json")
                .content("""{"username":"newusername","email":"newemail@example.com","password":"Newpassword123"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().json("""{"message":"Failed to update","id":null,"username":null,"email":null,"roles":[]}"""))
    }
}
