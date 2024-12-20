package hampusborg.bankapp.core.domain

import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "transactions")
data class Transaction(
    @Id val id: String? = null,
    val fromAccountId: String,
    val toAccountId: String,
    val userId: String,
    val amount: Double,
    val timestamp: Long,
    val categoryId: TransactionCategory,
    val date: String
)