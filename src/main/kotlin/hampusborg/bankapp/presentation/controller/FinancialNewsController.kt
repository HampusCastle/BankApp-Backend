package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.FetchFinancialNewsRequest
import hampusborg.bankapp.application.dto.response.FinancialNewsDetailsResponse
import hampusborg.bankapp.application.service.FinancialNewsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/news")
class FinancialNewsController(private val financialNewsService: FinancialNewsService) {

    @GetMapping("/finance")
    fun getFinancialNews(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int,
        @RequestParam(defaultValue = "business") category: String
    ): ResponseEntity<List<FinancialNewsDetailsResponse>> {
        return try {
            val newsApiRequest = FetchFinancialNewsRequest(page = page, pageSize = pageSize, category = category)
            val news = financialNewsService.getFinancialNews(newsApiRequest)
            ResponseEntity.ok(news)
        } catch (e: Exception) {
            throw e
        }
    }
}