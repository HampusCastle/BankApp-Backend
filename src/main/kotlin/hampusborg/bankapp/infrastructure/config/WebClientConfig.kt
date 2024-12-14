package hampusborg.bankapp.infrastructure.config

import hampusborg.bankapp.application.exception.classes.RateLimitExceededException
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
class WebClientConfig {

    private val rateLimiter: RateLimiter = RateLimiter.of(
        "externalApiRateLimiter",
        RateLimiterConfig.custom()
            .limitForPeriod(5)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(500))
            .build()
    )

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://www.alphavantage.co")
            .filter { request, next ->
                val permission = rateLimiter.acquirePermission()
                if (permission) {
                    next.exchange(request)
                } else {
                    throw RateLimitExceededException("Rate limit exceeded for external API")
                }
            }
            .build()
    }
}