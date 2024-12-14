package hampusborg.bankapp.application.service.base

import org.springframework.stereotype.Service

@Service
class PaymentGatewayService {

    fun processPayment(fromAccountId: String, toAccountId: String, amount: Double): Boolean {
        println("Processing payment of $amount from $fromAccountId to $toAccountId")
        return true
    }
}