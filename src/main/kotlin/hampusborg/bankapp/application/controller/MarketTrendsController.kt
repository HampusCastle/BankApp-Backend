package hampusborg.bankapp.application.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/market-trends")
class MarketTrendsController(private val marketTrendsService: MarketTrendsService) {

    @Operation(summary = "Get market trends for a given symbol")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved market trends"),
            ApiResponse(responseCode = "400", description = "Invalid request"),
            ApiResponse(responseCode = "500", description = "Server error")
        ]
    )
    @PostMapping
    fun getMarketTrends(@RequestBody @Valid marketTrendsRequest: MarketTrendsRequest): ResponseEntity<MarketTrendsResponse> {
        val marketTrends = marketTrendsService.getMarketTrends(marketTrendsRequest)
        return ResponseEntity.ok(marketTrends)
    }
}
