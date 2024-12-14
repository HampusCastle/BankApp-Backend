package hampusborg.bankapp.presentation.controller

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
        @RequestBody query: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<String> {
        if (query.isBlank()) {
            return ResponseEntity.badRequest().body("Query cannot be empty")
        }

        val userDetails = jwtUtil.extractUserDetails(token.substringAfter(" "))
        val userId = userDetails?.first

        return if (userId != null) {
            val response = chatbotService.getChatbotResponse(query, userId)
            if (response.isBlank()) {
                ResponseEntity.ok("No information available")
            } else {
                ResponseEntity.ok(response)
            }
        } else {
            ResponseEntity.badRequest().body("Invalid token or user.")
        }
    }
}