package hampusborg.bankapp.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import hampusborg.bankapp.application.dto.request.CreateScheduledPaymentRequest
import hampusborg.bankapp.application.service.ScheduledPaymentService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.ScheduledPayment
import hampusborg.bankapp.infrastructure.config.SecurityConfig
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.Test

@WebMvcTest(ScheduledPaymentController::class)
@Import(SecurityConfig::class)
@WithMockUser(username = "user", roles = ["USER"])
class ScheduledPaymentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var scheduledPaymentService: ScheduledPaymentService

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var rateLimiterService: RateLimiterService

    @TestConfiguration
    class ScheduledPaymentServiceTestConfig {
        @Bean
        fun scheduledPaymentService(): ScheduledPaymentService = mock()

        @Bean
        fun jwtUtil(): JwtUtil = mock()

        @Bean
        fun rateLimiterService(): RateLimiterService = org.mockito.kotlin.mock()
    }

    @BeforeEach
    fun setup() {
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)  // Mock rate limiter behavior for all tests
    }

    @Test
    fun `should create scheduled payment successfully`() {
        val request = CreateScheduledPaymentRequest(
            fromAccountId = "fromAccount",
            toAccountId = "toAccount",
            amount = 100.0,
            nextPaymentDate = System.currentTimeMillis() + 86400000,
            schedule = "daily"
        )

        val response = ScheduledPayment(
            id = "123",
            userId = "user123",
            amount = 100.0,
            fromAccountId = "fromAccount",
            toAccountId = "toAccount",
            schedule = "daily",
            nextPaymentDate = System.currentTimeMillis() + 86400000
        )

        whenever(jwtUtil.extractUserDetails("mock_token")).thenReturn(Pair("user123", listOf("ROLE_USER")))
        whenever(scheduledPaymentService.createScheduledPayment(any(), eq("user123"))).thenReturn(response)

        mockMvc.perform(
            post("/scheduled-payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer mock_token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Scheduled payment created successfully for user: user123"))
    }

    @Test
    fun `should update scheduled payment successfully`() {
        val request = CreateScheduledPaymentRequest(
            fromAccountId = "fromAccount",
            toAccountId = "toAccount",
            amount = 200.0,
            nextPaymentDate = System.currentTimeMillis() + 86400000,
            schedule = "weekly"
        )

        val updatedPayment = ScheduledPayment(
            id = "123",
            userId = "user123",
            amount = 200.0,
            fromAccountId = "fromAccount",
            toAccountId = "toAccount",
            schedule = "weekly",
            nextPaymentDate = System.currentTimeMillis() + 86400000
        )

        whenever(scheduledPaymentService.updateScheduledPayment(eq("123"), any())).thenReturn(updatedPayment)

        mockMvc.perform(
            put("/scheduled-payments/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Scheduled payment updated successfully."))
    }

    @Test
    fun `should delete scheduled payment successfully`() {
        mockMvc.perform(delete("/scheduled-payments/123"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Scheduled payment deleted successfully"))
    }

    @Test
    fun `should handle bad request when invalid data is provided`() {
        val invalidRequest = CreateScheduledPaymentRequest(
            fromAccountId = "",
            toAccountId = "",
            amount = 0.0,
            nextPaymentDate = System.currentTimeMillis() + 86400000,
            schedule = "daily"
        )

        mockMvc.perform(
            post("/scheduled-payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("Authorization", "Bearer mock_token")
        )
            .andExpect(status().isBadRequest)
            .andDo { result -> println(result.response.contentAsString) }
    }

    @Test
    fun `should handle unauthorized request without token`() {
        val request = CreateScheduledPaymentRequest(
            fromAccountId = "fromAccount",
            toAccountId = "toAccount",
            amount = 100.0,
            nextPaymentDate = System.currentTimeMillis() + 86400000,
            schedule = "daily"
        )

        mockMvc.perform(
            post("/scheduled-payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andDo { result -> println(result.response.contentAsString) }
    }
}