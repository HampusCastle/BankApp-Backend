package hampusborg.bankapp.application.service.base

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RateLimiterService {

    private val rateLimiter: RateLimiter = RateLimiterRegistry.of(
        RateLimiterConfig.custom()
            .limitForPeriod(5)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(500))
            .build()
    ).rateLimiter("userRateLimiter")

    fun isAllowed(userId: String): Boolean {
        return rateLimiter.acquirePermission()
    }
}