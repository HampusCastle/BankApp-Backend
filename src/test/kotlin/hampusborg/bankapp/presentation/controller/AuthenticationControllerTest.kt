package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.AuthenticateUserRequest
import hampusborg.bankapp.application.dto.request.RegisterUserRequest
import hampusborg.bankapp.application.dto.response.RegisteredUserResponse
import hampusborg.bankapp.application.exception.classes.AccountCreationException
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.application.service.AuthenticationService
import hampusborg.bankapp.application.service.base.RateLimiterService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(AuthenticationController::class)
class AuthenticationControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var authenticationService: AuthenticationService

    @Autowired var rateLimiterService: RateLimiterService = mock()

    @TestConfiguration
    class AuthenticationServiceTestConfig {
        @Bean
        fun authenticationService(): AuthenticationService = mock()
        @Bean
        fun rateLimiterService(): RateLimiterService = org.mockito.kotlin.mock()
    }

    @BeforeEach
    fun setup() {
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)
    }
    @Test
    @WithMockUser
    fun `should register user successfully`() {
        val registerUserRequest = RegisterUserRequest(
            username = "testuser",
            password = "password123",
            email = "testuser@example.com"
        )
        val registeredUserResponse = RegisteredUserResponse(
            id = "12345",
            username = "testuser",
            roles = listOf("USER")
        )

        whenever(authenticationService.registerUser(any())).thenReturn(registeredUserResponse)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "username": "testuser",
                        "password": "password123",
                        "email": "testuser@example.com"
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("12345"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0]").value("USER"))
    }

    @Test
    @WithMockUser
    fun `should return bad request when username already exists`() {
        whenever(authenticationService.registerUser(any())).thenThrow(DuplicateUserException("Username already exists"))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "username": "testuser",
                        "password": "password123",
                        "email": "testuser@example.com"
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `should return internal server error when account creation fails`() {
        whenever(authenticationService.registerUser(any())).thenThrow(AccountCreationException("Failed to create account"))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "username": "testuser",
                        "password": "password123",
                        "email": "testuser@example.com"
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
    }

    @Test
    @WithMockUser
    fun `should authenticate user successfully and return token`() {
        val authenticateUserRequest = AuthenticateUserRequest(username = "testuser", password = "password123")
        val token = "valid.jwt.token"

        whenever(authenticationService.loginUser(any())).thenReturn(token)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                    "username": "testuser",
                    "password": "password123"
                }
                """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.token").value(token))
    }

    @Test
    @WithMockUser
    fun `should return unauthorized when invalid credentials are provided`() {
        whenever(authenticationService.loginUser(any())).thenThrow(RuntimeException("Invalid username or password"))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                    "username": "testuser",
                    "password": "wrongpassword"
                }
                """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("Invalid username or password"))
    }
}