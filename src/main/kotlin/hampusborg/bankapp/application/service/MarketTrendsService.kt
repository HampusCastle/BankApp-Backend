package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.GetMarketTrendsRequest
import hampusborg.bankapp.application.dto.response.MarketTrendsDetailsResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MarketTrendsService(
    @Value("\${financial.api.key}") private val financialApiKey: String,
    private val restTemplate: RestTemplate 
) {


    fun getMarketTrends(request: GetMarketTrendsRequest): MarketTrendsDetailsResponse {
        val userId = "marketTrendsUser"

        val url = buildUrl(request.symbol)
        return try {
            val response = fetchMarketData(url)
            val quote = extractQuote(response)
            val trend = calculateTrend(quote["change"]?.toDoubleOrNull() ?: 0.0)

            MarketTrendsDetailsResponse(
                trend = trend,
                price = quote["price"] ?: "N/A",
                volume = quote["volume"] ?: "N/A",
                changePercent = quote["changePercent"] ?: "N/A"
            )
        } catch (e: ApiRequestException) {
            throw e
        } catch (e: Exception) {
            throw ApiRequestException("Failed to fetch market trends: ${e.message ?: "Unknown error"}")
        }
    }

    private fun buildUrl(symbol: String): String {
        return "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=$symbol&apikey=$financialApiKey"
    }

    private fun fetchMarketData(url: String): Map<String, Map<String, String>> {
        val response = restTemplate.getForObject(url, Map::class.java) as Map<String, Map<String, String>>?
            ?: throw ApiRequestException("Empty response from API")

        return response
    }

    private fun extractQuote(response: Map<String, Map<String, String>>): Map<String, String> {
        return response["Global Quote"]
            ?: throw ApiRequestException("Missing 'Global Quote' data in the API response")
    }

    private fun calculateTrend(change: Double): String {
        return if (change > 0) "The market has gone up today!" else "The market has gone down today."
    }
}