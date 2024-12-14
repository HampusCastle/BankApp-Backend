package hampusborg.bankapp.infrastructure.filter

import hampusborg.bankapp.application.service.base.RateLimiterService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class RateLimitingFilter(
    private val rateLimiterService: RateLimiterService
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userId = getUserIdFromRequest(request)

        if (!rateLimiterService.isAllowed(userId)) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Too many requests, please try again later.")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun getUserIdFromRequest(request: HttpServletRequest): String {
        return request.getHeader("userId") ?: "defaultUser"
    }
}