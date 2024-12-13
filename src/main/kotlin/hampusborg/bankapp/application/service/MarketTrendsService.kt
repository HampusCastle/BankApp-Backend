package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.MarketTrendsRequest
import hampusborg.bankapp.application.dto.response.MarketTrendsResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class MarketTrendsService(
    @Value("\${financial.api.key}") private val financialApiKey: String,
    private val webClient: WebClient
) {

    fun getMarketTrends(request: MarketTrendsRequest): MarketTrendsResponse {
        val symbol = request.symbol
        val url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=$symbol&apikey=$financialApiKey"

        return try {
            val response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<Map<String, Map<String, String>>>() {})
                .block() ?: throw ApiRequestException("Failed to fetch market trends: Empty response from API")

            val quote = response["Global Quote"]
                ?: throw ApiRequestException("Failed to fetch market trends: Missing 'Global Quote' data in the API response")

            val change = quote["change"]?.toDoubleOrNull() ?: 0.0
            val price = quote["price"] ?: "N/A"
            val volume = quote["volume"] ?: "N/A"
            val percentageChange = quote["changePercent"] ?: "N/A"

            MarketTrendsResponse(
                trend = if (change > 0) "The market has gone up today!" else "The market has gone down today.",
                price = price,
                volume = volume,
                changePercent = percentageChange
            )
        } catch (e: Exception) {
            throw ApiRequestException("${e.message}")
        }
    }
}
