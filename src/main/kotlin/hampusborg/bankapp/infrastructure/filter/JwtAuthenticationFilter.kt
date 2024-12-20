package hampusborg.bankapp.infrastructure.filter

import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authHeader = request.getHeader("Authorization")
        val token = authHeader?.takeIf { it.startsWith("Bearer ") }?.substring(7)

        if (token != null && jwtUtil.isValidToken(token)) {
            val (userId, roles) = jwtUtil.extractUserDetails(token)
                ?: throw IllegalArgumentException("Invalid token details")

            val authentication = UsernamePasswordAuthenticationToken(
                userId, null, roles.map { SimpleGrantedAuthority(it) }
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}