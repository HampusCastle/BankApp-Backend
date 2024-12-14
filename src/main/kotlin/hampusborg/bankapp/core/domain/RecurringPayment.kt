package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recurring_payments")
data class RecurringPayment(
    @Id val id: String? = null,
    val userId: String,
    var amount: Double,
    val fromAccountId: String,
    var toAccountId: String,
    var interval: String,
    var categoryId: String,
    var status: String = "active",
    var nextPaymentDate: Long
)