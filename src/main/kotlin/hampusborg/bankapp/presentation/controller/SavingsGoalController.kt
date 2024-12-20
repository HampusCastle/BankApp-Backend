package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.dto.response.SavingsGoalDetailsResponse
import hampusborg.bankapp.application.service.SavingsGoalService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/savings-goals")
class SavingsGoalController(
    private val savingsGoalService: SavingsGoalService
) {

    @PostMapping
    fun createSavingsGoal(@Valid @RequestBody request: CreateSavingsGoalRequest, authentication: Authentication): ResponseEntity<SavingsGoalDetailsResponse> {
        val userId = authentication.principal as String

        val createdGoal = savingsGoalService.createSavingsGoal(request, userId)
        return ResponseEntity.ok(createdGoal)
    }

    @GetMapping("/user/{userId}/savings-goals")
    fun getSavingsGoalsByUser(@PathVariable userId: String): ResponseEntity<List<SavingsGoalDetailsResponse>> {
        val savingsGoals = savingsGoalService.getSavingsGoalsByUserId(userId)
        return if (savingsGoals.isNotEmpty()) {
            ResponseEntity.ok(savingsGoals)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping("/{id}")
    fun getSavingsGoalById(@PathVariable id: String): ResponseEntity<SavingsGoalDetailsResponse> {
        val goal = savingsGoalService.getSavingsGoal(id)
        return ResponseEntity.ok(goal)
    }

    @GetMapping("/user/{userId}")
    fun getSavingsGoalsByUserId(@PathVariable userId: String): ResponseEntity<List<SavingsGoalDetailsResponse>> {
        val goals = savingsGoalService.getSavingsGoalsByUserId(userId)
        return ResponseEntity.ok(goals)
    }

    @PatchMapping("/{id}")
    fun updateSavingsGoal(
        @PathVariable id: String,
        @RequestBody updatedFields: Map<String, Any>
    ): ResponseEntity<SavingsGoalDetailsResponse> {
        val updatedGoal = savingsGoalService.updateSavingsGoal(id, updatedFields)
        return ResponseEntity.ok(updatedGoal)
    }

    @DeleteMapping("/{id}")
    fun deleteSavingsGoal(@PathVariable id: String): ResponseEntity<Void> {
        savingsGoalService.deleteSavingsGoal(id)
        return ResponseEntity.noContent().build()
    }
}