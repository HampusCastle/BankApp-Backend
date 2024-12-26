package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.UserActivityLog
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserActivityLogRepository : MongoRepository<UserActivityLog, String> {
    fun findByUserId(userId: String): List<UserActivityLog>
    fun findByAction(action: String): List<UserActivityLog>
    fun findByTimestampBetween(start: Long, end: Long): List<UserActivityLog>
}
