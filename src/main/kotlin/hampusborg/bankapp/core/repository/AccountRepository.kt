package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.Account
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : MongoRepository<Account, String> {
    fun findByUserId(userId: String): List<Account>
    fun findByAccountType(accountType: String): List<Account>
}