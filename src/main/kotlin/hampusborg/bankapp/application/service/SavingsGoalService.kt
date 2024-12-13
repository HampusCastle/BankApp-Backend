package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.exception.classes.SavingsGoalNotFoundException
import hampusborg.bankapp.core.domain.SavingsGoal
import hampusborg.bankapp.core.repository.SavingsGoalRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SavingsGoalService(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    private val logger = LoggerFactory.getLogger(SavingsGoalService::class.java)

    fun createSavingsGoal(savingsGoal: SavingsGoal): SavingsGoal {
        logger.info("Creating savings goal: ${savingsGoal.name} for user: ${savingsGoal.userId}")
        return savingsGoalRepository.save(savingsGoal)
    }

    fun getSavingsGoal(id: String): SavingsGoal {
        logger.info("Fetching savings goal with ID: $id")
        return savingsGoalRepository.findById(id).orElseThrow {
            logger.error("Savings goal not found for ID: $id")
            throw SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
    }

    fun updateSavingsGoal(id: String, savingsGoal: SavingsGoal): SavingsGoal {
        logger.info("Updating savings goal with ID: $id")
        val existingGoal = savingsGoalRepository.findById(id).orElseThrow {
            logger.error("Savings goal not found for ID: $id")
            throw SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
        existingGoal.currentAmount += savingsGoal.currentAmount
        return savingsGoalRepository.save(existingGoal)
    }

    fun deleteSavingsGoal(id: String) {
        logger.info("Deleting savings goal with ID: $id")
        val goal = savingsGoalRepository.findById(id).orElseThrow {
            logger.error("Savings goal not found for ID: $id")
            throw SavingsGoalNotFoundException("Savings goal not found for ID: $id")
        }
        savingsGoalRepository.delete(goal)
    }

    fun getSavingsGoalsByUserId(userId: String): List<SavingsGoal> {
        logger.info("Fetching all savings goals for user ID: $userId")
        return savingsGoalRepository.findAllByUserId(userId)
    }
}