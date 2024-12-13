package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.TransactionCategory
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionCategoryRepository : MongoRepository<TransactionCategory, String> {
    fun findByName(name: String): TransactionCategory?
}