package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.Transaction
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : MongoRepository<Transaction, String> {
    fun findByFromAccountIdAndUserId(fromAccountId: String, userId: String): List<Transaction>
    fun findByToAccountIdAndUserId(toAccountId: String, userId: String): List<Transaction>
    fun findByUserId(userId: String): List<Transaction>
}