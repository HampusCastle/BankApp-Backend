package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.application.service.base.RateLimiterService
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.springframework.stereotype.Service

@Service
class SavingsGoalService(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val paymentService: PaymentService,
    private val rateLimiterService: RateLimiterService,
    private val cacheHelperService: CacheHelperService  // Inject CacheHelperService
) {
    fun createSavingsGoal(savingsGoal: SavingsGoal): SavingsGoal {
        if (!rateLimiterService.isAllowed(savingsGoal.userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        return savingsGoalRepository.save(savingsGoal)
    }

    fun getSavingsGoal(id: String): SavingsGoal {
        if (!rateLimiterService.isAllowed(id)) {
            throw Exception("Too many requests, please try again later.")
        }

        return cacheHelperService.getSavingsGoal(id)  // Use CacheHelperService for caching logic
    }

    fun getSavingsGoalsByUserId(userId: String): List<SavingsGoal> {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        return cacheHelperService.getSavingsGoalsByUserId(userId)  // Use CacheHelperService for caching
    }

    fun updateSavingsGoal(id: String, savingsGoal: SavingsGoal): SavingsGoal {
        if (!rateLimiterService.isAllowed(id)) {
            throw Exception("Too many requests, please try again later.")
        }

        val existingGoal = savingsGoalRepository.findById(id).orElseThrow {
            throw SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
        existingGoal.currentAmount += savingsGoal.currentAmount
        return savingsGoalRepository.save(existingGoal)
    }

    fun deleteSavingsGoal(id: String) {
        if (!rateLimiterService.isAllowed(id)) {
            throw Exception("Too many requests, please try again later.")
        }

        val goal = savingsGoalRepository.findById(id).orElseThrow {
            throw SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
        savingsGoalRepository.delete(goal)
    }

    fun allocateFundsToSavingsGoal(
        savingsGoalId: String,
        fromAccountId: String,
        amount: Double,
        userId: String
    ): SavingsGoal {
        if (!rateLimiterService.isAllowed(userId)) {
            throw Exception("Too many requests, please try again later.")
        }

        val savingsGoal = savingsGoalRepository.findById(savingsGoalId).orElseThrow {
            throw SavingsGoalNotFoundException("Savings goal not found for ID: $savingsGoalId")
        }

        val transferRequest = InitiateTransferRequest(
            fromAccountId = fromAccountId,
            toAccountId = savingsGoal.accountId,
            amount = amount,
            categoryId = null
        )

        paymentService.handleTransfer(transferRequest, userId)

        savingsGoal.currentAmount += amount
        savingsGoalRepository.save(savingsGoal)

        return savingsGoal
    }
}