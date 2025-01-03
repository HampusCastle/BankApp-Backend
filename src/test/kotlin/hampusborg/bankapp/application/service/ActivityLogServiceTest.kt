package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.UserActivityLog
import hampusborg.bankapp.core.repository.UserActivityLogRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.mockito.kotlin.argThat

class ActivityLogServiceTest {

    private val userActivityLogRepository: UserActivityLogRepository = mock()
    private val activityLogService = ActivityLogService(userActivityLogRepository)

    @Test
    fun `should log activity successfully`() {
        val userId = "testUser"
        val action = "Test Action"
        val details = "Test details"

        activityLogService.logActivity(userId, action, details)

        verify(userActivityLogRepository, times(1)).save(argThat {
            assertEquals(userId, this.userId)
            assertEquals(action, this.action)
            assertEquals(details, this.details)
            true
        })
    }

    @Test
    fun `should get logs by user ID`() {
        val log = UserActivityLog(userId = "testUser", action = "Test Action", timestamp = 123456789)
        whenever(userActivityLogRepository.findByUserId("testUser")).thenReturn(listOf(log))

        val logs = activityLogService.getLogsByUserId("testUser")

        assertEquals(1, logs.size)
        assertEquals("Test Action", logs[0].action)
        verify(userActivityLogRepository, times(1)).findByUserId("testUser")
    }

    @Test
    fun `should get all logs`() {
        val log = UserActivityLog(userId = "testUser", action = "Test Action", timestamp = 123456789)
        whenever(userActivityLogRepository.findAll()).thenReturn(listOf(log))

        val logs = activityLogService.getAllLogs()

        assertEquals(1, logs.size)
        assertEquals("Test Action", logs[0].action)
        verify(userActivityLogRepository, times(1)).findAll()
    }
}