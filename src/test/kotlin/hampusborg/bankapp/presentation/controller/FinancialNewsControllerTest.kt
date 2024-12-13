package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.FinancialNewsRequest
import hampusborg.bankapp.application.dto.response.FinancialNewsResponse
import hampusborg.bankapp.application.service.FinancialNewsService
import hampusborg.bankapp.presentation.controller.FinancialNewsController
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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

    @TestConfiguration
    class FinancialNewsServiceTestConfig {
        @Bean
        fun financialNewsService(): FinancialNewsService = mock()
    }

    @Test
    @WithMockUser
    fun `should return financial news successfully`() {
        val mockNews = listOf(
            FinancialNewsResponse("Finance News 1", "Description of finance news 1", "Source 1", "http://link1.com"),
            FinancialNewsResponse("Finance News 2", "Description of finance news 2", "Source 2", "http://link2.com")
        )

        val financialNewsRequest = FinancialNewsRequest(page = 1, pageSize = 5, category = "business")

        whenever(financialNewsService.getFinancialNews(financialNewsRequest)).thenReturn(mockNews)

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