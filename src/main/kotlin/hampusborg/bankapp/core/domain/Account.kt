package hampusborg.bankapp.core.domain

import hampusborg.bankapp.core.domain.enums.AccountType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "accounts")
data class Account(
    @Id val id: String? = null,

    @field:Indexed val userId: String,

    val name: String,
    val accountType: AccountType,
    var balance: Double,
    val interestRate: Double? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)