package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.response.ChatbotResponse
import hampusborg.bankapp.application.service.ChatbotService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chatbot")
class ChatbotController(
    private val chatbotService: ChatbotService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/query")
    fun handleChatbotQuery(
        @RequestBody query: Map<String, String>,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ChatbotResponse> {
        val queryText = query["query"] ?: return createErrorResponse("Query cannot be empty")

        if (!token.startsWith("Bearer ")) {
            return createErrorResponse("Authorization header must start with 'Bearer '.")
        }

        val userId = jwtUtil.extractUserDetails(token.substringAfter("Bearer "))?.first
            ?: return createErrorResponse("Invalid or missing token.")

        val response = chatbotService.getChatbotResponse(queryText, userId)
        return ResponseEntity.ok(response)
    }

    private fun createErrorResponse(message: String): ResponseEntity<ChatbotResponse> {
        return ResponseEntity.badRequest().body(ChatbotResponse("Error", message))
    }
}
