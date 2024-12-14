package hampusborg.bankapp.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.dto.response.SavingsGoalDetailsResponse
import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.application.service.SavingsGoalService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.SavingsGoal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SavingsGoalController::class)
class SavingsGoalControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var savingsGoalService: SavingsGoalService

    @Autowired
    private lateinit var rateLimiterService: RateLimiterService

    @TestConfiguration
    class SavingsGoalServiceTestConfig {
        @Bean
        fun savingsGoalService(): SavingsGoalService = mock()
        @Bean
        fun rateLimiterService(): RateLimiterService = mock()
    }

    @BeforeEach
    fun setup() {
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)  // Mock rate limiter behavior for all tests
    }
    @Test
    fun `should create savings goal`() {
        val request = CreateSavingsGoalRequest(
            name = "Vacation Fund",
            userId = "user123",
            targetAmount = 5000.0,
            targetDate = LocalDate.of(2025, 1, 1),
            accountId = "123"
        )
        val response = SavingsGoalDetailsResponse(
            id = "goal123",
            name = request.name,
            userId = request.userId,
            targetAmount = request.targetAmount,
            targetDate = request.targetDate,
            currentAmount = 0.0
        )

        whenever(savingsGoalService.createSavingsGoal(any())).thenReturn(
            SavingsGoal(
                id = response.id,
                name = response.name,
                userId = response.userId,
                targetAmount = response.targetAmount,
                targetDate = response.targetDate,
                currentAmount = response.currentAmount,
                accountId = "123"
            )
        )

        mockMvc.perform(
            post("/savings-goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("goal123"))
            .andExpect(jsonPath("$.name").value("Vacation Fund"))
    }

    @Test
    fun `should fetch savings goal by ID`() {
        val response = SavingsGoalDetailsResponse(
            id = "goal123",
            name = "Vacation Fund",
            userId = "user123",
            targetAmount = 5000.0,
            targetDate = LocalDate.of(2025, 1, 1),
            currentAmount = 1000.0
        )

        whenever(savingsGoalService.getSavingsGoal("goal123")).thenReturn(
            SavingsGoal(
                id = response.id,
                name = response.name,
                userId = response.userId,
                targetAmount = response.targetAmount,
                targetDate = response.targetDate,
                currentAmount = response.currentAmount,
                accountId = "123"
            )
        )

        mockMvc.perform(get("/savings-goals/goal123"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("goal123"))
            .andExpect(jsonPath("$.name").value("Vacation Fund"))
            .andExpect(jsonPath("$.currentAmount").value(1000.0))
    }

    @Test
    fun `should return bad request for invalid savings goal creation`() {
        val invalidRequest = CreateSavingsGoalRequest(
            name = "",
            userId = "",
            targetAmount = -1000.0,
            targetDate = LocalDate.of(2020, 1, 1),
            accountId = "123"
        )

        mockMvc.perform(
            post("/savings-goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.name").value("Goal name cannot be empty"))
            .andExpect(jsonPath("$.errors.targetAmount").value("Target amount must be greater than 0"))
            .andExpect(jsonPath("$.errors.userId").value("User ID cannot be empty"))
    }

    @Test
    fun `should return not found for non-existent savings goal`() {
        whenever(savingsGoalService.getSavingsGoal("nonExistentId"))
            .thenThrow(SavingsGoalNotFoundException("Savings goal not found"))

        mockMvc.perform(get("/savings-goals/nonExistentId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value("NOT_FOUND"))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Savings goal not found"))
    }

    @Test
    fun `should fetch all savings goals for a user`() {
        val savingsGoals = listOf(
            SavingsGoal("goal1", "Goal 1", "user123", 1000.0, LocalDate.of(2025, 1, 1), 200.0, accountId = "123"),
            SavingsGoal("goal2", "Goal 2", "user123", 5000.0, LocalDate.of(2026, 1, 1), 1000.0, accountId = "123")
        )

        whenever(savingsGoalService.getSavingsGoalsByUserId("user123")).thenReturn(savingsGoals)

        mockMvc.perform(get("/savings-goals").param("userId", "user123"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("goal1"))
            .andExpect(jsonPath("$[1].id").value("goal2"))
    }

    @Test
    fun `should update savings goal`() {
        val updateRequest = CreateSavingsGoalRequest(
            name = "Updated Goal",
            userId = "user123",
            targetAmount = 6000.0,
            targetDate = LocalDate.of(2025, 12, 31),
            accountId = "123"
        )
        val updatedResponse = SavingsGoalDetailsResponse(
            id = "goal123",
            name = updateRequest.name,
            userId = updateRequest.userId,
            targetAmount = updateRequest.targetAmount,
            targetDate = updateRequest.targetDate,
            currentAmount = 500.0
        )

        whenever(savingsGoalService.updateSavingsGoal(eq("goal123"), any())).thenReturn(
            SavingsGoal(
                id = updatedResponse.id,
                name = updatedResponse.name,
                userId = updatedResponse.userId,
                targetAmount = updatedResponse.targetAmount,
                targetDate = updatedResponse.targetDate,
                currentAmount = updatedResponse.currentAmount,
                accountId = "123"
            )
        )

        mockMvc.perform(
            put("/savings-goals/goal123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated Goal"))
            .andExpect(jsonPath("$.targetAmount").value(6000.0))
            .andExpect(jsonPath("$.currentAmount").value(500.0))
    }

    @Test
    fun `should delete savings goal`() {
        doNothing().whenever(savingsGoalService).deleteSavingsGoal("goal123")

        mockMvc.perform(delete("/savings-goals/goal123"))
            .andExpect(status().isNoContent)
    }
}