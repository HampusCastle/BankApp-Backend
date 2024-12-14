package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.dto.response.SavingsProgressSummaryResponse
import hampusborg.bankapp.application.service.BudgetService
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/budget-reports")
class BudgetController(
    private val budgetService: BudgetService
) {

    @GetMapping("/expenses/{userId}")
    fun getMonthlyExpenses(@PathVariable @NotEmpty userId: String): ResponseEntity<ExpensesSummaryResponse> {
        val totalExpenses = budgetService.getMonthlyExpenses(userId)
        return ResponseEntity.ok(totalExpenses)
    }

    @GetMapping("/savings-progress/{userId}/{savingsGoalId}")
    fun getSavingsProgress(
        @PathVariable @NotEmpty userId: String,
        @PathVariable @NotEmpty savingsGoalId: String
    ): ResponseEntity<SavingsProgressSummaryResponse> {
        val progress = budgetService.getSavingsProgress(userId, savingsGoalId)
        return ResponseEntity.ok(progress)
    }
}