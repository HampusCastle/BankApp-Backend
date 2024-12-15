package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.UserActivityLog
import hampusborg.bankapp.core.repository.UserActivityLogRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals

@TestPropertySource(locations = ["classpath:application-test.properties"])
class ActivityLogServiceTest {

    private val userActivityLogRepository: UserActivityLogRepository = mock()
    private val activityLogService = ActivityLogService(userActivityLogRepository)

    @Test
    fun `should log activity successfully`() {
        val userId = "user123"
        val action = "Logged In"
        val details = "User logged in from the mobile app"
        val log = UserActivityLog(userId = userId, action = action, timestamp = System.currentTimeMillis(), details = details)

        whenever(userActivityLogRepository.save(any<UserActivityLog>())).thenReturn(log)

        activityLogService.logActivity(userId, action, details)

        verify(userActivityLogRepository).save(argThat {
            this.userId == userId &&
                    this.action == action &&
                    this.details == details
        })
    }

    @Test
    fun `should get logs by userId successfully`() {
        val userId = "user123"
        val logs = listOf(
            UserActivityLog(userId = userId, action = "Logged In", timestamp = System.currentTimeMillis(), details = "Login successful"),
            UserActivityLog(userId = userId, action = "Viewed Profile", timestamp = System.currentTimeMillis(), details = "User viewed profile")
        )

        whenever(userActivityLogRepository.findAll()).thenReturn(logs)

        val result = activityLogService.getLogsByUserId(userId)

        assertEquals(2, result.size)
        verify(userActivityLogRepository).findAll()
    }

    @Test
    fun `should get logs by action successfully`() {
        val action = "Logged In"
        val logs = listOf(
            UserActivityLog(userId = "user123", action = action, timestamp = System.currentTimeMillis(), details = "Login successful"),
            UserActivityLog(userId = "user456", action = action, timestamp = System.currentTimeMillis(), details = "Login from mobile")
        )

        whenever(userActivityLogRepository.findAll()).thenReturn(logs)

        val result = activityLogService.getLogsByAction(action)

        assertEquals(2, result.size)
        verify(userActivityLogRepository).findAll()
    }

    @Test
    fun `should get logs by timestamp range successfully`() {
        val start = System.currentTimeMillis() - 10000
        val end = System.currentTimeMillis()
        val logs = listOf(
            UserActivityLog(userId = "user123", action = "Logged In", timestamp = System.currentTimeMillis(), details = "Login successful")
        )

        whenever(userActivityLogRepository.findAll()).thenReturn(logs)

        val result = activityLogService.getLogsByTimestampRange(start, end)

        assertEquals(1, result.size)
        verify(userActivityLogRepository).findAll()
    }

    @Test
    fun `should get all logs successfully`() {
        val logs = listOf(
            UserActivityLog(userId = "user123", action = "Logged In", timestamp = System.currentTimeMillis(), details = "Login successful")
        )

        whenever(userActivityLogRepository.findAll()).thenReturn(logs)

        val result = activityLogService.getAllLogs()

        assertEquals(1, result.size)
        verify(userActivityLogRepository).findAll()
    }
}