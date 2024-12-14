package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.util.InterestCalculator
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class InterestService(
    private val accountRepository: AccountRepository
) {
    @Scheduled(cron = "0 0 1 * * ?")
    fun applyInterest() {
        val savingsAccounts = accountRepository.findByAccountType("Savings")

        if (savingsAccounts.isEmpty()) return

        savingsAccounts.forEach { account ->
            try {
                val interest = InterestCalculator.calculateInterest(account.balance, account.interestRate)
                if (interest > 0) {
                    account.balance += interest
                    accountRepository.save(account)
                }
            } catch (e: Exception) {
            }
        }
    }
}