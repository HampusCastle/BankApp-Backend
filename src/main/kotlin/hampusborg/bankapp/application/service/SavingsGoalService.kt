package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.dto.response.SavingsGoalDetailsResponse
import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SavingsGoalService(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val cacheHelperService: CacheHelperService
) {

    fun createSavingsGoal(request: CreateSavingsGoalRequest, userId: String): SavingsGoalDetailsResponse {
        val savingsGoal = SavingsGoal(
            name = request.name,
            userId = userId,
            targetAmount = request.targetAmount,
            targetDate = request.targetDate
        )
        val savedSavingsGoal = savingsGoalRepository.save(savingsGoal)

        cacheHelperService.storeSavingsGoal(savedSavingsGoal.id!!, savedSavingsGoal)
        cacheHelperService.evictCache("savingsGoals", userId)

        return mapToResponse(savedSavingsGoal)
    }

    fun updateSavingsGoal(id: String, updatedFields: Map<String, Any>): SavingsGoalDetailsResponse {
        val savingsGoal = cacheHelperService.getCache("savingsGoalDetails", id, SavingsGoal::class.java)
            ?: savingsGoalRepository.findById(id).orElseThrow {
                SavingsGoalNotFoundException("Savings goal not found for ID: $id")
            }

        updatedFields["currentAmount"]?.let {
            savingsGoal.currentAmount = (it as Number).toDouble()
        }
        updatedFields["targetAmount"]?.let { savingsGoal.targetAmount = (it as Number).toDouble() }
        updatedFields["targetDate"]?.let { savingsGoal.targetDate = LocalDate.parse(it as String) }
        updatedFields["name"]?.let { savingsGoal.name = it as String }

        val updatedSavingsGoal = savingsGoalRepository.save(savingsGoal)
        cacheHelperService.storeSavingsGoal(updatedSavingsGoal.id!!, updatedSavingsGoal)
        cacheHelperService.evictSavingsGoalsForUser(savingsGoal.userId)

        return mapToResponse(updatedSavingsGoal)
    }

    fun getSavingsGoal(id: String): SavingsGoalDetailsResponse {
        val savingsGoal = cacheHelperService.getCache("savingsGoalDetails", id, SavingsGoal::class.java)
            ?: savingsGoalRepository.findById(id).orElseThrow {
                SavingsGoalNotFoundException("Savings goal not found for ID: $id")
            }

        cacheHelperService.storeSavingsGoal(savingsGoal.id!!, savingsGoal)
        return mapToResponse(savingsGoal)
    }

    fun getSavingsGoalsByUserId(userId: String): List<SavingsGoalDetailsResponse> {
        val cachedGoals = cacheHelperService.getCache("savingsGoals", userId, List::class.java)
            ?.filterIsInstance<SavingsGoal>()
        if (!cachedGoals.isNullOrEmpty()) {
            return cachedGoals.map { mapToResponse(it) }
        }

        val savingsGoals = savingsGoalRepository.findByUserId(userId)
        cacheHelperService.storeSavingsGoalsForUser(userId, savingsGoals)

        return savingsGoals.map { mapToResponse(it) }
    }

    fun deleteSavingsGoal(id: String) {
        val goal = savingsGoalRepository.findById(id).orElseThrow {
            SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }

        savingsGoalRepository.delete(goal)
        cacheHelperService.evictSavingsGoalsForUser(goal.userId)
    }

    private fun mapToResponse(savingsGoal: SavingsGoal): SavingsGoalDetailsResponse {
        return SavingsGoalDetailsResponse(
            id = savingsGoal.id!!,
            name = savingsGoal.name,
            userId = savingsGoal.userId,
            targetAmount = savingsGoal.targetAmount,
            targetDate = savingsGoal.targetDate,
            currentAmount = savingsGoal.currentAmount
        )
    }
}