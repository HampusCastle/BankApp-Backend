package hampusborg.bankapp.application.controller

import jakarta.validation.constraints.NotEmpty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transaction-categories")
class TransactionCategoryController(
    private val transactionCategoryService: TransactionCategoryService
) {

    @PostMapping
    fun createCategory(
        @RequestParam @NotEmpty name: String,
        @RequestParam(required = false) description: String?
    ): ResponseEntity<TransactionCategory> {
        return try {
            val category = transactionCategoryService.createCategory(name, description)
            ResponseEntity.ok(category)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(null)
        }
    }

    @GetMapping
    fun getAllCategories(): ResponseEntity<List<TransactionCategory>> {
        return ResponseEntity.ok(transactionCategoryService.getAllCategories())
    }
}