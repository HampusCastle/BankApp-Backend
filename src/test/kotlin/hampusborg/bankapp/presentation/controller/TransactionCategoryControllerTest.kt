package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.service.TransactionCategoryService
import hampusborg.bankapp.core.domain.TransactionCategory
import hampusborg.bankapp.presentation.controller.TransactionCategoryController
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TransactionCategoryController::class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionCategoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var transactionCategoryService: TransactionCategoryService

    @TestConfiguration
    class TransactionCategoryServiceTestConfig {
        @Bean
        fun transactionCategoryService(): TransactionCategoryService = mock()
    }

    @Test
    fun `should create category successfully`() {
        val category = TransactionCategory(id = "1", name = "Food", description = "Food related expenses")
        whenever(transactionCategoryService.createCategory("Food", "Food related expenses")).thenReturn(category)

        mockMvc.perform(
            post("/transaction-categories")
                .param("name", "Food")
                .param("description", "Food related expenses")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.name").value("Food"))
    }

    @Test
    fun `should return bad request for invalid category creation`() {
        mockMvc.perform(
            post("/transaction-categories")
                .param("name", "")
                .param("description", "")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should fetch all categories successfully`() {
        val categories = listOf(
            TransactionCategory(id = "1", name = "Food", description = "Food related expenses"),
            TransactionCategory(id = "2", name = "Transport", description = "Transport related expenses")
        )
        whenever(transactionCategoryService.getAllCategories()).thenReturn(categories)

        mockMvc.perform(get("/transaction-categories"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(2))
    }
}