package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressSummaryResponse
import hampusborg.bankapp.application.service.BudgetService
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.validation.annotation.Validated

@RestController
@RequestMapping("/budget-reports")
@Validated
class BudgetController(
    private val budgetService: BudgetService
) {
    @GetMapping("/expenses/{userId}/{accountId}")
    fun getMonthlyExpenses(
        @PathVariable @NotEmpty userId: String,
        @PathVariable @NotEmpty accountId: String
    ): ResponseEntity<ExpensesSummaryResponse> {
        return try {
            val totalExpenses = budgetService.getMonthlyExpenses(userId, accountId)
            ResponseEntity.ok(totalExpenses)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExpensesSummaryResponse(totalExpenses = 0.0, categories = emptyMap()))
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