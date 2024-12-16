package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.CreateSavingsGoalRequest
import hampusborg.bankapp.application.dto.response.SavingsGoalDetailsResponse
import hampusborg.bankapp.application.service.SavingsGoalService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/savings-goals")
class SavingsGoalController(
    private val savingsGoalService: SavingsGoalService
) {

    @PostMapping
    fun createSavingsGoal(@Valid @RequestBody request: CreateSavingsGoalRequest): ResponseEntity<SavingsGoalDetailsResponse> {
        val createdGoal = savingsGoalService.createSavingsGoal(request)
        return ResponseEntity.ok(createdGoal)
    }

    @GetMapping("/user/{userId}/savings-goals")
    fun getSavingsGoalsByUser(@PathVariable userId: String): ResponseEntity<List<SavingsGoalDetailsResponse>> {
        val savingsGoals = savingsGoalService.getSavingsGoalsByUserId(userId)
        return ResponseEntity.ok(savingsGoals)
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