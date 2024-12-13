package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.TransferRequest
import hampusborg.bankapp.application.dto.response.TransferResponse
import hampusborg.bankapp.application.mapper.TransactionMapper
import hampusborg.bankapp.application.service.TransferService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import hampusborg.bankapp.presentation.controller.TransferController
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TransferController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TransactionMapper::class)
class TransferControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var transferService: TransferService

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @TestConfiguration
    class TransferServiceTestConfig {
        @Bean
        fun transferService(): TransferService = mock()

        @Bean
        fun jwtUtil(): JwtUtil = mock()
    }

    @Test
    fun `should transfer funds successfully`() {
        val transferRequest = TransferRequest(
            fromAccountId = "acc1",
            toAccountId = "acc2",
            amount = 100.0,
            categoryId = "category1"
        )

        val transferResponse = TransferResponse(message = "Transfer successful", status = "success")

        whenever(transferService.transferFunds(any(), any())).thenReturn(transferResponse)
        whenever(jwtUtil.extractUserDetails("valid_token")).thenReturn(Pair("user123", listOf("ROLE_USER")))

        mockMvc.perform(
            post("/transfers")
                .header("Authorization", "Bearer valid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"fromAccountId":"acc1", "toAccountId":"acc2", "amount":100.0, "categoryId":"category1"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Transfer successful"))
            .andExpect(jsonPath("$.status").value("success"))
    }

    @Test
    fun `should return bad request when transfer fails`() {
        val transferRequest = TransferRequest(
            fromAccountId = "acc1",
            toAccountId = "acc2",
            amount = 100.0,
            categoryId = "category1"
        )

        val transferResponse = TransferResponse(message = "Transfer failed", status = "failed")

        whenever(transferService.transferFunds(any(), any())).thenThrow(RuntimeException("Transfer failed"))
        whenever(jwtUtil.extractUserDetails("valid_token")).thenReturn(Pair("user123", listOf("ROLE_USER")))

        mockMvc.perform(
            post("/transfers")
                .header("Authorization", "Bearer valid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"fromAccountId":"acc1", "toAccountId":"acc2", "amount":100.0, "categoryId":"category1"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Transfer failed"))
            .andExpect(jsonPath("$.status").value("failed"))
    }

    @Test
    fun `should return bad request when invalid data is provided`() {
        mockMvc.perform(
            post("/transfers")
                .header("Authorization", "Bearer valid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"fromAccountId":"", "toAccountId":"acc2", "amount":100.0, "categoryId":"category1"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.message").value("One or more fields have validation errors"))
            .andExpect(jsonPath("$.errors.fromAccountId").value("From account ID cannot be empty"))
    }
}