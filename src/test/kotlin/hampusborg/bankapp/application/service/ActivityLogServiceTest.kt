import hampusborg.bankapp.application.service.ActivityLogService
import hampusborg.bankapp.core.domain.UserActivityLog
import hampusborg.bankapp.core.repository.UserActivityLogRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class ActivityLogServiceTest {

    private val logRepository: UserActivityLogRepository = mock(UserActivityLogRepository::class.java)
    private val activityLogService: ActivityLogService = ActivityLogService(logRepository)

    @Test
    fun `should log activity successfully`() {
        val log = UserActivityLog(userId = "testUser", action = "Test Action", timestamp = 2024-12-12)
        `when`(logRepository.save(any(UserActivityLog::class.java))).thenReturn(log)
        activityLogService.logActivity("testUser", "Test Action")
        verify(logRepository, times(1)).save(any(UserActivityLog::class.java))
    }

    @Test
    fun `should get all logs`() {
        `when`(logRepository.findAll()).thenReturn(listOf(UserActivityLog(userId = "testUser", action = "Test Action", timestamp = 2024-12-12)))
        val logs = activityLogService.getAllLogs()
        assertEquals(1, logs.size)
    }
}