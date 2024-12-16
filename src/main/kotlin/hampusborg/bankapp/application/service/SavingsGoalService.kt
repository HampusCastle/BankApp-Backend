package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.dto.response.SavingsGoalDetailsResponse
import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.domain.enums.AccountType
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SavingsGoalService(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val accountRepository: AccountRepository,
    private val cacheHelperService: CacheHelperService
) {

    fun createSavingsGoal(request: CreateSavingsGoalRequest): SavingsGoalDetailsResponse {
        val account = Account(
            userId = request.userId,
            name = request.name,
            balance = 0.0,
            accountType = AccountType.SAVINGS,
            createdAt = LocalDateTime.now()
        )
        val savedAccount = accountRepository.save(account)

        val savingsGoal = SavingsGoal(
            name = request.name,
            userId = request.userId,
            targetAmount = request.targetAmount,
            targetDate = request.targetDate,
            accountId = savedAccount.id!!,
            currentAmount = 0.0
        )
        val savedSavingsGoal = savingsGoalRepository.save(savingsGoal)

        cacheHelperService.storeSavingsGoal(savedSavingsGoal.id!!, savedSavingsGoal)

        return SavingsGoalDetailsResponse(
            id = savedSavingsGoal.id,
            name = savedSavingsGoal.name,
            userId = savedSavingsGoal.userId,
            targetAmount = savedSavingsGoal.targetAmount,
            targetDate = savedSavingsGoal.targetDate,
            currentAmount = savedSavingsGoal.currentAmount,
            accountId = savedSavingsGoal.accountId
        )
    }

    fun getSavingsGoal(id: String): SavingsGoalDetailsResponse {
        val savingsGoal = savingsGoalRepository.findById(id).orElseThrow {
            SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
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

    fun getSavingsGoalsByUserId(userId: String): List<SavingsGoalDetailsResponse> {
        val savingsGoals = savingsGoalRepository.findByUserId(userId)
        return savingsGoals.map {
            SavingsGoalDetailsResponse(
                id = it.id!!,
                name = it.name,
                userId = it.userId,
                targetAmount = it.targetAmount,
                targetDate = it.targetDate,
                currentAmount = it.currentAmount,
                accountId = it.accountId
            )
        }
    }

    fun updateSavingsGoal(id: String, updatedFields: Map<String, Any>): SavingsGoalDetailsResponse {
        val savingsGoal = savingsGoalRepository.findById(id).orElseThrow {
            SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }

        updatedFields["name"]?.let { savingsGoal.name = it as String }
        updatedFields["targetAmount"]?.let { savingsGoal.targetAmount = (it as Number).toDouble() }
        updatedFields["targetDate"]?.let { savingsGoal.targetDate = LocalDate.parse(it as String) }
        updatedFields["currentAmount"]?.let { savingsGoal.currentAmount = (it as Number).toDouble() }

        val updatedSavingsGoal = savingsGoalRepository.save(savingsGoal)

        cacheHelperService.storeSavingsGoal(updatedSavingsGoal.id!!, updatedSavingsGoal)

        return SavingsGoalDetailsResponse(
            id = updatedSavingsGoal.id,
            name = updatedSavingsGoal.name,
            userId = updatedSavingsGoal.userId,
            targetAmount = updatedSavingsGoal.targetAmount,
            targetDate = updatedSavingsGoal.targetDate,
            currentAmount = updatedSavingsGoal.currentAmount,
            accountId = updatedSavingsGoal.accountId
        )
    }

    fun deleteSavingsGoal(id: String) {
        val goal = savingsGoalRepository.findById(id).orElseThrow {
            SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
        savingsGoalRepository.delete(goal)
    }
}