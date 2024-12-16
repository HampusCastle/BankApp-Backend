package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.service.FinancialNewsService
import hampusborg.bankapp.application.service.base.ExternalApiNewsHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/news")
class FinancialNewsController(
    private val financialNewsService: FinancialNewsService
) {

    @GetMapping("/finance")
    fun getFinancialNews(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int,
        @RequestParam(defaultValue = "business") category: String
    ): ResponseEntity<List<ExternalApiNewsHandler.FinancialNewsDetailsResponse>> {
        val news = financialNewsService.fetchFinancialNews(page, pageSize, category)
        return ResponseEntity.ok(news)
    }
}