package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.dto.response.SubscriptionResponse
import hampusborg.bankapp.application.service.SubscriptionService
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/{status}")
    fun getSubscriptionsByStatus(
        @PathVariable status: String,
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<List<SubscriptionResponse>> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
            ?: throw IllegalArgumentException("Invalid token")

        val subscriptions = subscriptionService.getSubscriptionsByStatus(userId, status)
        return ResponseEntity.ok(subscriptions)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSubscription(@RequestBody request: SubscriptionRequest): SubscriptionResponse {
        return subscriptionService.createSubscription(request)
    }

    @DeleteMapping("/{id}")
    fun cancelSubscription(@PathVariable id: String) {
        subscriptionService.cancelSubscription(id)
    }
}