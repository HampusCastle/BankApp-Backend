package hampusborg.bankapp.core.domain

import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "subscription")
data class Subscription(
    @Id val id: String? = null,
    val userId: String,
    var amount: Double,
    var serviceName: String,
    var interval: String,
    var categoryId: String = TransactionCategory.SUBSCRIPTIONS.name,
    var nextPaymentDate: Long,
    var status: String,
    val fromAccountId: String,
    val toAccountId: String,
    val date: LocalDateTime? = null,
)