package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.UserActivityLog
import hampusborg.bankapp.core.repository.UserActivityLogRepository
import org.springframework.stereotype.Service

@Service
class ActivityLogService(
    private val userActivityLogRepository: UserActivityLogRepository
) {
    fun logActivity(userId: String, action: String, details: String? = null) {
        val log = UserActivityLog(
            userId = userId,
            action = action,
            timestamp = System.currentTimeMillis(),
            details = details
        )
        userActivityLogRepository.save(log)
    }

    fun getLogsByUserId(userId: String): List<UserActivityLog> {
        return userActivityLogRepository.findByUserId(userId)
    }

    fun getAllLogs(): List<UserActivityLog> {
        return userActivityLogRepository.findAll()
    }
}