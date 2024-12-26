package hampusborg.bankapp.core.repository

import hampusborg.bankapp.core.domain.ScheduledPayment
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduledPaymentRepository : MongoRepository<ScheduledPayment, String> {
    fun findByUserId(userId: String): List<ScheduledPayment>
    fun findByNextPaymentDateBefore(currentTime: Long): List<ScheduledPayment>
}