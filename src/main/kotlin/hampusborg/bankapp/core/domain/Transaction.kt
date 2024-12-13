package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Transaction(
    @Id var id: String? = null,
    val fromAccountId: String,
    val toAccountId: String,
    val userId: String,
    val date : String,
    val amount: Double,
    val timestamp: Long,
    val categoryId: String
)