package hampusborg.bankapp.application.service

import hampusborg.bankapp.infrastructure.util.JwtUtil
import hampusborg.bankapp.presentation.controller.ChatbotController
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class ChatbotServiceTest {

    private val chatbotService: ChatbotService = mock()
    private val jwtUtil: JwtUtil = mock()
    private val chatbotController = ChatbotController(chatbotService, jwtUtil)

    @Test
    fun `should return response for valid query`() {
        val userId = "user123"
        val query = "What are the market trends?"
        val marketTrends = "Den globala marknaden har gått upp idag!"
        val token = "somevalidtoken"

        val roles = listOf("someRole")
        whenever(jwtUtil.extractUserDetails(token)).thenReturn(Pair(userId, roles))
        whenever(chatbotService.getChatbotResponse(query, userId)).thenReturn(marketTrends)

        val result = chatbotController.handleChatbotQuery(query, token)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(marketTrends, result.body)
    }

    @Test
    fun `should return bad request when token is invalid`() {
        val query = "What are the market trends?"
        val invalidToken = "invalidtoken"

        whenever(jwtUtil.extractUserDetails(invalidToken)).thenReturn(null)

        val result = chatbotController.handleChatbotQuery(query, invalidToken)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertEquals("Invalid token or user.", result.body)
    }

    @Test
    fun `should return bad request when token is missing`() {
        val query = "What are the market trends?"
        val result = chatbotController.handleChatbotQuery(query, "")

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertEquals("Invalid token or user.", result.body)
    }

    @Test
    fun `should return response for unrecognized query`() {
        val userId = "user123"
        val query = "Unrecognized query"
        val token = "somevalidtoken"
        val response = "Jag förstår inte din fråga. Försök att ställa en annan fråga."

        val roles = listOf("someRole")
        whenever(jwtUtil.extractUserDetails(token)).thenReturn(Pair(userId, roles))
        whenever(chatbotService.getChatbotResponse(query, userId)).thenReturn(response)

        val result = chatbotController.handleChatbotQuery(query, token)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(response, result.body)
    }

    @Test
    fun `should return bad request for empty query`() {
        val userId = "user123"
        val query = ""
        val token = "somevalidtoken"

        val roles = listOf("someRole")
        whenever(jwtUtil.extractUserDetails(token)).thenReturn(Pair(userId, roles))

        val result = chatbotController.handleChatbotQuery(query, token)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertEquals("Query cannot be empty", result.body)
    }

    @Test
    fun `should return empty response when service returns empty response`() {
        val userId = "user123"
        val query = "Show me my transactions"
        val token = "somevalidtoken"
        val emptyResponse = ""

        val roles = listOf("someRole")
        whenever(jwtUtil.extractUserDetails(token)).thenReturn(Pair(userId, roles))
        whenever(chatbotService.getChatbotResponse(query, userId)).thenReturn(emptyResponse)

        val result = chatbotController.handleChatbotQuery(query, token)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("No information available", result.body)
    }
}