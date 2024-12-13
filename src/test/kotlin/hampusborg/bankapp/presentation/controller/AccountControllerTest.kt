package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.AccountResponse
import hampusborg.bankapp.application.service.AccountService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import hampusborg.bankapp.presentation.controller.AccountController
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf

@WebMvcTest(AccountController::class)
class AccountControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var accountService: AccountService

    @Autowired
    lateinit var jwtUtil: JwtUtil

    @TestConfiguration
    class AccountServiceTestConfig {
        @Bean
        fun accountService(): AccountService = mock()

        @Bean
        fun jwtUtil(): JwtUtil = mock()
    }

    @Test
    @WithMockUser
    fun `should create account successfully`() {
        val token = "Bearer somevalidtoken"
        val accountResponse = AccountResponse(
            id = "account-id",
            name = "Test Account",
            balance = 1000.0,
            accountType = "Checking",
            userId = "user-id"
        )

        whenever(jwtUtil.extractUserDetails(token.substringAfter(" "))).thenReturn(Pair("user-id", listOf("USER")))
        whenever(accountService.createAccount(any(), eq("user-id"))).thenReturn(accountResponse)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/accounts/create")
                .header("Authorization", token)
                .contentType("application/json")
                .content(
                    """
                    {
                        "name": "Test Account", 
                        "accountType": "Checking", 
                        "balance": 1000.0
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("user-id"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balance").value(1000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.accountType").value("Checking"))
    }

    @Test
    @WithMockUser
    fun `should return bad request when account creation data is invalid`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/accounts/create")
                .header("Authorization", "Bearer somevalidtoken")
                .contentType("application/json")
                .content(
                    """
                    {
                        "name": "", 
                        "accountType": "", 
                        "balance": -1000.0
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `should return not found when account does not exist`() {
        val accountId = "non-existing-id"
        val token = "Bearer somevalidtoken"

        whenever(jwtUtil.extractUserDetails(token.substringAfter(" "))).thenReturn(Pair("user-id", listOf("USER")))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/accounts/$accountId")
                .header("Authorization", token)
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Not Found"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Account not found"))
    }

    @Test
    fun `should return unauthorized when token is missing`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/accounts/create")
                .contentType("application/json")
                .content(
                    """
                    {
                        "name": "Test Account", 
                        "accountType": "Checking", 
                        "balance": 1000.0
                    }
                    """.trimIndent()
                )
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `should return not found when deleting non-existing account`() {
        val accountId = "non-existing-id"
        val token = "Bearer somevalidtoken"

        whenever(jwtUtil.extractUserDetails(token.substringAfter(" "))).thenReturn(Pair("user-id", listOf("USER")))
        whenever(accountService.deleteAccount(accountId, "user-id")).thenReturn(false)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("/accounts/$accountId")
                .header("Authorization", token)
                .with(csrf())
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Not Found"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Account not found"))
    }
}