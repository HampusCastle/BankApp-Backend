package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.dto.response.SavingsGoalDetailsResponse
import hampusborg.bankapp.application.service.SavingsGoalService
import hampusborg.bankapp.core.domain.SavingsGoal
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/savings-goals")
class SavingsGoalController(
    private val savingsGoalService: SavingsGoalService
) {
    private val logger = LoggerFactory.getLogger(SavingsGoalController::class.java)

    @PostMapping
    fun createSavingsGoal(@Valid @RequestBody createSavingsGoalRequest: CreateSavingsGoalRequest): ResponseEntity<SavingsGoalDetailsResponse> {
        logger.info("Creating savings goal: ${createSavingsGoalRequest.name} for user: ${createSavingsGoalRequest.userId}")

        val savingsGoal = SavingsGoal(
            name = createSavingsGoalRequest.name,
            userId = createSavingsGoalRequest.userId,
            targetAmount = createSavingsGoalRequest.targetAmount,
            targetDate = createSavingsGoalRequest.targetDate,
            currentAmount = 0.0
        )

        val createdGoal = savingsGoalService.createSavingsGoal(savingsGoal)
        return ResponseEntity.ok(
            SavingsGoalDetailsResponse(
                id = createdGoal.id!!,
                name = createdGoal.name,
                userId = createdGoal.userId,
                targetAmount = createdGoal.targetAmount,
                targetDate = createdGoal.targetDate,
                currentAmount = createdGoal.currentAmount
            )
        )
    }

    @GetMapping("/{id}")
    fun getSavingsGoal(@PathVariable id: String): ResponseEntity<SavingsGoalDetailsResponse> {
        logger.info("Fetching savings goal with ID: $id")
        val goal = savingsGoalService.getSavingsGoal(id)
        return ResponseEntity.ok(
            SavingsGoalDetailsResponse(
                id = goal.id!!,
                name = goal.name,
                userId = goal.userId,
                targetAmount = goal.targetAmount,
                targetDate = goal.targetDate,
                currentAmount = goal.currentAmount
            )
        )
    }

    @GetMapping
    fun getSavingsGoalsByUserId(@RequestParam userId: String): ResponseEntity<List<SavingsGoalDetailsResponse>> {
        logger.info("Fetching all savings goals for user: $userId")
        val goals = savingsGoalService.getSavingsGoalsByUserId(userId).map { goal ->
            SavingsGoalDetailsResponse(
                id = goal.id!!,
                name = goal.name,
                userId = goal.userId,
                targetAmount = goal.targetAmount,
                targetDate = goal.targetDate,
                currentAmount = goal.currentAmount
            )
        }
        return ResponseEntity.ok(goals)
    }

    @PutMapping("/{id}")
    fun updateSavingsGoal(
        @PathVariable id: String,
        @Valid @RequestBody createSavingsGoalRequest: CreateSavingsGoalRequest
    ): ResponseEntity<SavingsGoalDetailsResponse> {
        logger.info("Updating savings goal with ID: $id")
        val updatedGoal = savingsGoalService.updateSavingsGoal(id, SavingsGoal(
            id = id,
            name = createSavingsGoalRequest.name,
            userId = createSavingsGoalRequest.userId,
            targetAmount = createSavingsGoalRequest.targetAmount,
            targetDate = createSavingsGoalRequest.targetDate,
            currentAmount = 0.0
        ))

        return ResponseEntity.ok(
            SavingsGoalDetailsResponse(
                id = updatedGoal.id!!,
                name = updatedGoal.name,
                userId = updatedGoal.userId,
                targetAmount = updatedGoal.targetAmount,
                targetDate = updatedGoal.targetDate,
                currentAmount = updatedGoal.currentAmount
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deleteSavingsGoal(@PathVariable id: String): ResponseEntity<Void> {
        logger.info("Deleting savings goal with ID: $id")
        savingsGoalService.deleteSavingsGoal(id)
        return ResponseEntity.noContent().build()
    }
}