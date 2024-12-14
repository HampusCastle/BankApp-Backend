package hampusborg.bankapp.core.domain


import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document
data class SavingsGoal(
    @Id val id: String? = null,
    var name: String,
    val userId: String,
    var targetAmount: Double,
    val targetDate: LocalDate,
    var currentAmount: Double = 0.0,
    val accountId: String,
)