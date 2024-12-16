package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.Transaction
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate



@Repository
interface TransactionRepository : MongoRepository<Transaction, String> {
    fun findByFromAccountId(fromAccountId: String): List<Transaction>
    fun findByToAccountId(toAccountId: String): List<Transaction>
    fun findByUserIdAndDateBetween(userId: String?, startDate: LocalDate?, endDate: LocalDate?): List<Transaction?>?
}