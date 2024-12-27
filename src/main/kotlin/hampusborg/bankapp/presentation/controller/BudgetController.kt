package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.ExpensesSummaryResponse
import hampusborg.bankapp.application.exception.classes.NoTransactionsFoundException
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

    @GetMapping("/expenses/{userId}")
    fun getMonthlyExpensesForAllAccounts(
        @PathVariable @NotEmpty userId: String
    ): ResponseEntity<ExpensesSummaryResponse> {
        return try {
            val totalExpenses = budgetService.getMonthlyExpensesForAllAccounts(userId)
            ResponseEntity.ok(totalExpenses)
        } catch (e: NoTransactionsFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExpensesSummaryResponse(totalExpenses = 0.0, categories = emptyMap()))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExpensesSummaryResponse(totalExpenses = 0.0, categories = emptyMap()))
        }
    }
}