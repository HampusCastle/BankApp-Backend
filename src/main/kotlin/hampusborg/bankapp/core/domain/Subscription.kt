package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Subscription(
    @Id val id: String? = null,
    val userId: String,
    var amount: Double,
    var serviceName: String,
    var interval: String,
    var categoryId: String?,
    var nextPaymentDate: Long,
    var status: String,
    val fromAccountId: String,
    val toAccountId: String
)