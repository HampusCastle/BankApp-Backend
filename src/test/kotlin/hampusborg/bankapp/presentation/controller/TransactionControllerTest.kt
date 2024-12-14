package hampusborg.bankapp.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import hampusborg.bankapp.application.dto.request.CreateTransactionRequest
import hampusborg.bankapp.application.mapper.TransactionMapper
import hampusborg.bankapp.application.service.TransactionService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.Transaction
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(TransactionController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TransactionMapper::class)
class TransactionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var transactionService: TransactionService

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var rateLimiterService: RateLimiterService

    @TestConfiguration
    class TransactionServiceTestConfig {
        @Bean
        fun transactionService(): TransactionService = mock()

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
    fun `should fetch transaction history successfully`() {
        val transactions = listOf(
            Transaction(id = "1", fromAccountId = "acc1", toAccountId = "acc2", userId = "user123", date = "2024-12-11", amount = 100.0, timestamp = System.currentTimeMillis(), categoryId = "1"),
            Transaction(id = "2", fromAccountId = "acc1", toAccountId = "acc2", userId = "user123", date = "2024-12-12", amount = 50.0, timestamp = System.currentTimeMillis(), categoryId = "2")
        )

        whenever(jwtUtil.extractUserDetails("valid_token")).thenReturn(Pair("user123", listOf("ROLE_USER")))
        whenever(transactionService.getTransactionHistory("user123")).thenReturn(transactions)

        mockMvc.perform(
            get("/transactions/history")
                .header("Authorization", "valid_token")
        )
            .andExpect(status().isOk)
            .andExpect { result ->
                val response = result.response.contentAsString
                assert(response.contains("id"))
                assert(response.contains("fromAccountId"))
                assert(response.contains("amount"))
            }
    }

    @Test
    fun `should return bad request when user id is not extracted from token`() {
        mockMvc.perform(
            get("/transactions/history")
                .header("Authorization", "invalid_token")
        )
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                val response = result.response.contentAsString
                assert(response.contains("User ID could not be extracted from token"))
            }
    }

    @Test
    fun `should create transaction successfully`() {
        val request = CreateTransactionRequest(
            fromAccountId = "fromAccount",
            toAccountId = "toAccount",
            amount = 100.0,
            categoryId = "1"
        )

        whenever(jwtUtil.extractUserDetails("valid_token")).thenReturn(Pair("user123", listOf("ROLE_USER")))

        mockMvc.perform(
            post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
                .header("Authorization", "Bearer valid_token")
        )
            .andExpect(status().isOk)
            .andExpect { result ->
                val response = result.response.contentAsString
                assert(response.contains("Transaction created successfully"))
                assert(response.contains("generatedId"))
            }
    }
}