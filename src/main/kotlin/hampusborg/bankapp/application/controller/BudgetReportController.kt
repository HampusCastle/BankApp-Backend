package hampusborg.bankapp.application.controller

import jakarta.validation.constraints.NotEmpty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/budget-reports")
class BudgetReportController(
    private val budgetReportService: BudgetReportService
) {

    @GetMapping("/expenses/{userId}")
    fun getMonthlyExpenses(@PathVariable @NotEmpty userId: String): ResponseEntity<MonthlyExpensesResponse> {
        val totalExpenses = budgetReportService.getMonthlyExpenses(userId)
        return ResponseEntity.ok(totalExpenses)
    }

    @GetMapping("/savings-progress/{userId}/{savingsGoalId}")
    fun getSavingsProgress(
        @PathVariable @NotEmpty userId: String,
        @PathVariable @NotEmpty savingsGoalId: String
    ): ResponseEntity<SavingsProgressResponse> {
        val progress = budgetReportService.getSavingsProgress(userId, savingsGoalId)
        return ResponseEntity.ok(progress)
    }
}