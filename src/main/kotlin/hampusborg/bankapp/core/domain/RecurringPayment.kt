package hampusborg.bankapp.core.domain

import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recurring_payment")
data class RecurringPayment(
    @Id val id: String? = null,
    val userId: String,
    var amount: Double,
    val fromAccountId: String,
    var toAccountId: String,
    var interval: String,
    var categoryId: TransactionCategory = TransactionCategory.RECURRING_PAYMENT,
    var status: String = "active",
    var nextPaymentDate: Long
)