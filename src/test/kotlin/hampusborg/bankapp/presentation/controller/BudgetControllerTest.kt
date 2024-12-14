package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressSummaryResponse
import hampusborg.bankapp.application.service.BudgetService
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(BudgetController::class)
class BudgetControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var budgetService: BudgetService

    @Autowired
    lateinit var rateLimiterService: RateLimiterService

    @TestConfiguration
    class BudgetReportServiceTestConfig {
        @Bean
        fun budgetReportService(): BudgetService = mock()
        @Bean
        fun rateLimiterService(): RateLimiterService = org.mockito.kotlin.mock()
    }

    @BeforeEach
    fun setup() {
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)  // Mock rate limiter behavior for all tests
    }
    @Test
    @WithMockUser
    fun `should return monthly expenses successfully`() {
        val userId = "12345"
        val response = ExpensesSummaryResponse(
            totalExpenses = 1000.0,
            categories = mapOf("Food" to 400.0, "Transport" to 200.0, "Entertainment" to 400.0)
        )

        whenever(budgetService.getMonthlyExpenses(userId)).thenReturn(response)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/budget-reports/expenses/$userId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalExpenses").value(1000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.categories.Food").value(400.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.categories.Transport").value(200.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.categories.Entertainment").value(400.0))
    }

    @Test
    @WithMockUser
    fun `should return savings progress successfully`() {
        val userId = "12345"
        val savingsGoalId = "67890"
        val response = SavingsProgressSummaryResponse(
            totalSaved = 5000.0,
            savingsGoal = 10000.0,
            progressPercentage = 50.0
        )

        whenever(budgetService.getSavingsProgress(userId, savingsGoalId)).thenReturn(response)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/budget-reports/savings-progress/$userId/$savingsGoalId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalSaved").value(5000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.savingsGoal").value(10000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.progressPercentage").value(50.0))
    }
}