package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.GetMarketTrendsRequest
import hampusborg.bankapp.application.dto.response.MarketTrendsDetailsResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class MarketTrendsServiceTest {

    @Value("\${financial.api.key}")
    private lateinit var financialApiKey: String

    private lateinit var webClient: WebClient
    private lateinit var marketTrendsService: MarketTrendsService

    @BeforeEach
    fun setUp() {
        webClient = mock()
        marketTrendsService = MarketTrendsService(financialApiKey, webClient)
    }

    private fun mockWebClient(mockResponse: Map<String, Map<String, String>>) {
        val requestHeadersUriSpec = mock<RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mock<RequestHeadersSpec<*>>()
        val responseSpec = mock<ResponseSpec>()

        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(any<String>())).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(eq(object : ParameterizedTypeReference<Map<String, Map<String, String>>>() {})))
            .thenReturn(Mono.just(mockResponse))
    }

    @Test
    fun `should return market trends when API call is successful`() {
        val mockResponse = mapOf(
            "Global Quote" to mapOf(
                "change" to "1.2",
                "price" to "100.0",
                "volume" to "5000",
                "changePercent" to "1.2%"
            )
        )

        mockWebClient(mockResponse)

        val result = marketTrendsService.getMarketTrends(GetMarketTrendsRequest(symbol = "SPY"))

        val expected = MarketTrendsDetailsResponse(
            trend = "The market has gone up today!",
            price = "100.0",
            volume = "5000",
            changePercent = "1.2%"
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return market down message when price change is negative`() {
        val mockResponse = mapOf(
            "Global Quote" to mapOf(
                "change" to "-1.2",
                "price" to "100.0",
                "volume" to "5000",
                "changePercent" to "-1.2%"
            )
        )

        mockWebClient(mockResponse)

        val result = marketTrendsService.getMarketTrends(GetMarketTrendsRequest(symbol = "SPY"))

        val expected = MarketTrendsDetailsResponse(
            trend = "The market has gone down today.",
            price = "100.0",
            volume = "5000",
            changePercent = "-1.2%"
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should throw exception when API call fails`() {
        val requestHeadersUriSpec = mock<RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mock<RequestHeadersSpec<*>>()
        val responseSpec = mock<ResponseSpec>()

        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(any<String>())).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(eq(object : ParameterizedTypeReference<Map<String, Map<String, String>>>() {})))
            .thenThrow(RuntimeException("API error"))

        val exception = assertFailsWith<ApiRequestException> {
            marketTrendsService.getMarketTrends(GetMarketTrendsRequest(symbol = "SPY"))
        }

        assertEquals("API error", exception.message)
    }

    @Test
    fun `should throw exception when response is missing Global Quote`() {
        val mockResponse = emptyMap<String, Map<String, String>>()

        mockWebClient(mockResponse)

        val exception = assertFailsWith<ApiRequestException> {
            marketTrendsService.getMarketTrends(GetMarketTrendsRequest(symbol = "SPY"))
        }

        assertEquals("Failed to fetch market trends: Missing 'Global Quote' data in the API response", exception.message)
    }
}
