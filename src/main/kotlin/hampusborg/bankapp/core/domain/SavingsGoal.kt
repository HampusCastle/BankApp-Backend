package hampusborg.bankapp.core.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "savings_goals")
data class SavingsGoal(
    @Id
    val id: String? = null,
    var name: String,
    val userId: String,
    var targetAmount: Double,
    var currentAmount: Double = 0.0,
    var targetDate: LocalDate,
    val accountId: String
)