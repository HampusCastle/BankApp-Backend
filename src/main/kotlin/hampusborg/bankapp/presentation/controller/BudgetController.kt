package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressSummaryResponse
import hampusborg.bankapp.application.service.BudgetService
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/budget-reports")
class BudgetController(
    private val budgetService: BudgetService
) {
    @GetMapping("/expenses/{userId}")
    fun getMonthlyExpenses(@PathVariable @NotEmpty userId: String): ResponseEntity<ExpensesSummaryResponse> {
        return try {
            val totalExpenses = budgetService.getMonthlyExpenses(userId)
            ResponseEntity.ok(totalExpenses)
        } catch (e: Exception) {
            throw e
        }
    }

    @GetMapping("/savings-progress/{userId}/{savingsGoalId}")
    fun getSavingsProgress(
        @PathVariable @NotEmpty userId: String,
        @PathVariable @NotEmpty savingsGoalId: String
    ): ResponseEntity<SavingsProgressSummaryResponse> {
        return try {
            val progress = budgetService.getSavingsProgress(userId, savingsGoalId)
            ResponseEntity.ok(progress)
        } catch (e: Exception) {
            throw e
        }
    }
}