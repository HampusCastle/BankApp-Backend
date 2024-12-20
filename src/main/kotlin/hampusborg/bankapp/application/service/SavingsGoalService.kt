package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.dto.response.SavingsGoalDetailsResponse
import hampusborg.bankapp.application.exception.classes.InvalidAccountException
import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.domain.enums.AccountType
import hampusborg.bankapp.core.domain.enums.TransactionCategory
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SavingsGoalService(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val accountRepository: AccountRepository,
    private val cacheHelperService: CacheHelperService,
    private val paymentService: PaymentService
) {

    fun createSavingsGoal(request: CreateSavingsGoalRequest, userId: String): SavingsGoalDetailsResponse {
        val account = Account(
            userId = userId,
            name = request.name,
            balance = 0.0,
            accountType = AccountType.SAVINGS,
            createdAt = LocalDateTime.now()
        )
        val savedAccount = accountRepository.save(account)

        val savingsGoal = SavingsGoal(
            name = request.name,
            userId = userId,
            targetAmount = request.targetAmount,
            targetDate = request.targetDate,
            accountId = savedAccount.id!!,
            currentAmount = 0.0
        )
        val savedSavingsGoal = savingsGoalRepository.save(savingsGoal)

        paymentService.logTransaction(
            fromAccountId = "DEPOSIT",
            toAccountId = savedAccount.id!!,
            userId = userId,
            amount = 0.0,
            category = TransactionCategory.SAVINGS_GOAL
        )

        cacheHelperService.storeSavingsGoal(savedSavingsGoal.id!!, savedSavingsGoal)

        return mapToResponse(savedSavingsGoal)
    }

    fun updateSavingsGoal(id: String, updatedFields: Map<String, Any>): SavingsGoalDetailsResponse {
        val savingsGoal = savingsGoalRepository.findById(id).orElseThrow {
            SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }

        val account = accountRepository.findById(savingsGoal.accountId).orElseThrow {
            InvalidAccountException("Account not found for ID: ${savingsGoal.accountId}")
        }
        if (account.accountType != AccountType.SAVINGS) {
            throw InvalidAccountException("The account linked to this savings goal must be a SAVINGS account.")
        }

        updatedFields["currentAmount"]?.let {
            val previousAmount = savingsGoal.currentAmount
            savingsGoal.currentAmount = (it as Number).toDouble()

            paymentService.logTransaction(
                fromAccountId = "USER_INPUT",
                toAccountId = savingsGoal.accountId,
                userId = savingsGoal.userId,
                amount = savingsGoal.currentAmount - previousAmount,
                category = TransactionCategory.SAVINGS_GOAL
            )
        }

        updatedFields["targetAmount"]?.let { savingsGoal.targetAmount = (it as Number).toDouble() }
        updatedFields["targetDate"]?.let { savingsGoal.targetDate = LocalDate.parse(it as String) }
        updatedFields["name"]?.let { savingsGoal.name = it as String }

        val updatedSavingsGoal = savingsGoalRepository.save(savingsGoal)

        cacheHelperService.storeSavingsGoal(updatedSavingsGoal.id!!, updatedSavingsGoal)

        return mapToResponse(updatedSavingsGoal)
    }

    private fun mapToResponse(savingsGoal: SavingsGoal): SavingsGoalDetailsResponse {
        return SavingsGoalDetailsResponse(
            id = savingsGoal.id!!,
            name = savingsGoal.name,
            userId = savingsGoal.userId,
            targetAmount = savingsGoal.targetAmount,
            targetDate = savingsGoal.targetDate,
            currentAmount = savingsGoal.currentAmount,
            accountId = savingsGoal.accountId
        )
    }

    fun getSavingsGoal(id: String): SavingsGoalDetailsResponse {
        val savingsGoal = savingsGoalRepository.findById(id).orElseThrow {
            SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
        return mapToResponse(savingsGoal)
    }

    fun getSavingsGoalsByUserId(userId: String): List<SavingsGoalDetailsResponse> {
        println("Fetching savings goals for userId: $userId")
        val savingsGoals = savingsGoalRepository.findByUserId(userId)
        println("Found savings goals: $savingsGoals")
        return savingsGoalRepository.findByUserId(userId).map { mapToResponse(it) }
    }

    fun deleteSavingsGoal(id: String) {
        val goal = savingsGoalRepository.findById(id).orElseThrow {
            SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
        savingsGoalRepository.delete(goal)
    }
}