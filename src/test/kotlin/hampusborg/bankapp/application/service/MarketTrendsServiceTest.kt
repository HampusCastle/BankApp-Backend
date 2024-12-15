package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.GetMarketTrendsRequest
import hampusborg.bankapp.application.dto.response.MarketTrendsDetailsResponse
import hampusborg.bankapp.application.exception.classes.ApiRequestException
import hampusborg.bankapp.application.service.base.RateLimiterService
import io.github.cdimascio.dotenv.Dotenv
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.TestPropertySource
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class MarketTrendsServiceTest {

    @Value("\${financial.api.key}")
    private lateinit var financialApiKey: String

    private lateinit var webClient: WebClient
    private lateinit var rateLimiterService: RateLimiterService
    private lateinit var marketTrendsService: MarketTrendsService


    @BeforeEach
    fun setUp() {
        webClient = mock()
        rateLimiterService = mock()
        marketTrendsService = MarketTrendsService(financialApiKey, webClient, rateLimiterService)
        }

    private fun mockWebClient(mockResponse: Map<String, Map<String, String>>) {
        val requestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mock<WebClient.ResponseSpec>()

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
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)

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
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)

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
        val requestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mock<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mock<WebClient.ResponseSpec>()

        whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
        whenever(requestHeadersUriSpec.uri(any<String>())).thenReturn(requestHeadersSpec)
        whenever(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        whenever(responseSpec.bodyToMono(eq(object : ParameterizedTypeReference<Map<String, Map<String, String>>>() {})))
            .thenThrow(RuntimeException("API error"))

        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)

        val exception = assertFailsWith<ApiRequestException> {
            marketTrendsService.getMarketTrends(GetMarketTrendsRequest(symbol = "SPY"))
        }

        assertEquals("Failed to fetch market trends: API error", exception.message)
    }

    @Test
    fun `should throw exception when response is missing Global Quote`() {
        val mockResponse = emptyMap<String, Map<String, String>>()

        mockWebClient(mockResponse)
        whenever(rateLimiterService.isAllowed(any())).thenReturn(true)

        val exception = assertFailsWith<ApiRequestException> {
            marketTrendsService.getMarketTrends(GetMarketTrendsRequest(symbol = "SPY"))
        }

        assertEquals("Missing 'Global Quote' data in the API response", exception.message)
    }
}