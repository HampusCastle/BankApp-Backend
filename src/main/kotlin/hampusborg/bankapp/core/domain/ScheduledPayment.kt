package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "scheduled_payment")
data class ScheduledPayment(
    @Id val id: String? = null,
    val userId: String,
    var amount: Double,
    val fromAccountId: String,
    val toAccountId: String,
    var schedule: String,
    var nextPaymentDate: Long,
    val categoryId: String? = null,
)