package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.RecurringPayment
import org.springframework.data.mongodb.repository.MongoRepository

interface RecurringPaymentRepository : MongoRepository<RecurringPayment, String> {

    fun findByUserId(userId: String): List<RecurringPayment>

    fun findByNextPaymentDateBeforeAndStatus(nextPaymentDate: Long, status: String): List<RecurringPayment>

}