package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.FetchFinancialNewsRequest
import hampusborg.bankapp.application.dto.response.FinancialNewsDetailsResponse
import hampusborg.bankapp.application.service.FinancialNewsService
import hampusborg.bankapp.application.service.base.RateLimiterService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(FinancialNewsController::class)
class FinancialNewsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var financialNewsService: FinancialNewsService

    @Autowired
    lateinit var rateLimiterService: RateLimiterService

    @TestConfiguration
    class FinancialNewsServiceTestConfig {
        @Bean
        fun financialNewsService(): FinancialNewsService = mock()
        @Bean
        fun rateLimiterService(): RateLimiterService = mock()
    }

    @BeforeEach
    fun setup() {
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)
    }
    @Test
    @WithMockUser
    fun `should return financial news successfully`() {
        val mockNews = listOf(
            FinancialNewsDetailsResponse("Finance News 1", "Description of finance news 1", "Source 1", "http://link1.com"),
            FinancialNewsDetailsResponse("Finance News 2", "Description of finance news 2", "Source 2", "http://link2.com")
        )

        val fetchFinancialNewsRequest = FetchFinancialNewsRequest(page = 1, pageSize = 5, category = "business")

        whenever(financialNewsService.getFinancialNews(fetchFinancialNewsRequest)).thenReturn(mockNews)

        val response = mockMvc.perform(get("/news/finance")
            .param("page", "1")
            .param("pageSize", "5")
            .param("category", "business"))
            .andReturn().response

        assertEquals(HttpStatus.OK.value(), response.status)

        assertTrue(response.contentAsString.contains("Finance News 1"))
        assertTrue(response.contentAsString.contains("Finance News 2"))
    }
}