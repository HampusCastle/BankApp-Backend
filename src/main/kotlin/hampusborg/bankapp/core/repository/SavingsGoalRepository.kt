package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.SavingsGoal
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SavingsGoalRepository : MongoRepository<SavingsGoal, String> {
    fun findAllByUserId(userId: String): List<SavingsGoal>

}