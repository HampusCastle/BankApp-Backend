package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "notification")
data class Notification(
    @Id val id: String? = null,
    val userId: String,
    val message: String,
    val timestamp: Long,
    val type: String
)