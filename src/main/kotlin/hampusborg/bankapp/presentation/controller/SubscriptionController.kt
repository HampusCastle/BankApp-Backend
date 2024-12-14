package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.dto.response.SubscriptionResponse
import hampusborg.bankapp.application.service.SubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSubscription(@RequestBody request: SubscriptionRequest): SubscriptionResponse {
        return subscriptionService.createSubscription(request)
    }

    @GetMapping("/{id}")
    fun getSubscription(@PathVariable id: String): SubscriptionResponse {
        return subscriptionService.getSubscriptionById(id)
    }

    @PutMapping("/{id}")
    fun updateSubscription(
        @PathVariable id: String,
        @RequestBody request: SubscriptionRequest
    ): SubscriptionResponse {
        return subscriptionService.updateSubscription(id, request)
    }

    @DeleteMapping("/{id}")
    fun cancelSubscription(@PathVariable id: String) {
        subscriptionService.cancelSubscription(id)
    }

    @GetMapping("/user/{userId}")
    fun getSubscriptionsByUser(@PathVariable userId: String): List<SubscriptionResponse> {
        return subscriptionService.getSubscriptionsByUserId(userId)
    }
}