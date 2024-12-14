package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.FetchFinancialNewsRequest
import hampusborg.bankapp.application.dto.response.FinancialNewsDetailsResponse
import hampusborg.bankapp.application.service.FinancialNewsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/news")
class FinancialNewsController(private val financialNewsService: FinancialNewsService) {

    @GetMapping("/finance")
    fun getFinancialNews(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int,
        @RequestParam(defaultValue = "business") category: String
    ): ResponseEntity<List<FinancialNewsDetailsResponse>> {
        val newsApiRequest = FetchFinancialNewsRequest(page = page, pageSize = pageSize, category = category)
        val news = financialNewsService.getFinancialNews(newsApiRequest)
        return ResponseEntity.ok(news)
    }
}