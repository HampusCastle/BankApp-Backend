package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class SavingsGoalServiceTest {

    private val repository: SavingsGoalRepository = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val service = SavingsGoalService(repository, cacheHelperService)

    @Test
    fun `should create savings goal successfully`() {
        val request = CreateSavingsGoalRequest(
            name = "Vacation",
            targetAmount = 5000.0,
            targetDate = LocalDate.of(2025, 12, 31),
        )
        val savedGoal = SavingsGoal(
            id = "1",
            name = "Vacation",
            userId = "testUser",
            targetAmount = 5000.0,
            targetDate = LocalDate.of(2025, 12, 31),
        )

        whenever(repository.save(any())).thenReturn(savedGoal)

        val result = service.createSavingsGoal(request, "testUser")

        assertEquals("1", result.id)
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should fetch savings goals by user ID`() {
        val savedGoal = SavingsGoal(
            id = "1",
            name = "Vacation",
            userId = "testUser",
            targetAmount = 5000.0,
            targetDate = LocalDate.of(2025, 12, 31),
        )

        whenever(repository.findByUserId("testUser")).thenReturn(listOf(savedGoal))

        val result = service.getSavingsGoalsByUserId("testUser")

        assertEquals(1, result.size)
        assertEquals("Vacation", result[0].name)
        verify(repository, times(1)).findByUserId("testUser")
    }
}