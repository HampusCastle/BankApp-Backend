package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.TransactionCategory
import hampusborg.bankapp.core.repository.TransactionCategoryRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals

class TransactionCategoryServiceTest {

    private val transactionCategoryRepository: TransactionCategoryRepository = mock()
    private val transactionCategoryService = TransactionCategoryService(transactionCategoryRepository)

    @Test
    fun `should create category successfully`() {
        val categoryName = "Groceries"
        val categoryDescription = "Daily grocery expenses"
        val category = TransactionCategory(name = categoryName, description = categoryDescription)

        whenever(transactionCategoryRepository.save(any<TransactionCategory>())).thenReturn(category)

        val result = transactionCategoryService.createCategory(categoryName, categoryDescription)

        assertEquals(categoryName, result.name)
        assertEquals(categoryDescription, result.description)
        verify(transactionCategoryRepository).save(any<TransactionCategory>())
    }

    @Test
    fun `should retrieve all categories successfully`() {
        val categories = listOf(
            TransactionCategory(name = "Groceries", description = "Grocery items"),
            TransactionCategory(name = "Entertainment", description = "Movies and events")
        )

        whenever(transactionCategoryRepository.findAll()).thenReturn(categories)

        val result = transactionCategoryService.getAllCategories()

        assertEquals(2, result.size)
        verify(transactionCategoryRepository).findAll()
    }
}