package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.utility.InterestCalculator
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class InterestService(
    private val accountRepository: AccountRepository
) {
    private val logger = LoggerFactory.getLogger(InterestService::class.java)

    @Scheduled(cron = "0 0 1 * * ?")
    fun applyInterest() {
        val savingsAccounts = accountRepository.findByAccountType("Savings")
        logger.info("Applying interest to ${savingsAccounts.size} savings accounts.")

        if (savingsAccounts.isEmpty()) {
            logger.info("No savings accounts found for interest application.")
        }

        savingsAccounts.forEach { account ->
            try {
                val interest = InterestCalculator.calculateInterest(account.balance, account.interestRate)
                if (interest > 0) {
                    account.balance += interest
                    accountRepository.save(account)
                    logger.info("Applied interest of $interest to account ${account.id}. New balance: ${account.balance}.")
                } else {
                    logger.warn("No interest applied for account ${account.id} (interest rate is zero or null).")
                }
            } catch (e: Exception) {
                logger.error("Failed to apply interest for account ${account.id}: ${e.message}")
            }
        }
    }
}