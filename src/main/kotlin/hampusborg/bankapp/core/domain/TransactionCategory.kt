package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class TransactionCategory(
    @Id val id: String? = null,
    val name: String,
    val description: String? = null
)