package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.GetMarketTrendsRequest
import hampusborg.bankapp.application.dto.response.MarketTrendsDetailsResponse
import hampusborg.bankapp.application.service.MarketTrendsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/market-trends")
class MarketTrendsController(
    private val marketTrendsService: MarketTrendsService
) {

    @Operation(summary = "Get market trends for a given symbol")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved market trends"),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "500", description = "Server error")
        ]
    )
    @PostMapping
    fun getMarketTrends(@RequestBody @Valid getMarketTrendsRequest: GetMarketTrendsRequest): ResponseEntity<MarketTrendsDetailsResponse> {
        return try {
            val marketTrends = marketTrendsService.getMarketTrends(getMarketTrendsRequest)
            ResponseEntity.ok(marketTrends)
        } catch (e: Exception) {
            throw IllegalStateException("Error fetching market trends: ${e.message}", e)
        }
    }
}
