package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user_activity_log")
data class UserActivityLog(
    @Id val id: String? = null,
    val userId: String,
    val action: String,
    val timestamp: Long,
    val details: String? = null
)