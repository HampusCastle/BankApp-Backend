package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Account(
    @Id val id: String? = null,

    @field:Indexed val userId: String,

    val accountType: String,
    var balance: Double,
    val interestRate: Double? = null
)