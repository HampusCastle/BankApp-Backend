package hampusborg.bankapp.infrastructure.filter

import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtAuthenticationFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        println("Authorization Header: $authHeader")

        val token = authHeader?.takeIf { it.startsWith("Bearer ") }?.substring(7)
        println("Extracted Token: $token")

        if (token != null) {
            try {
                if (jwtUtil.isValidToken(token)) {
                    val (userId, roles) = jwtUtil.extractUserDetails(token)
                        ?: throw IllegalArgumentException("Invalid token details")

                    println("UserID: $userId, Roles: $roles") // Debug log

                    val authentication = UsernamePasswordAuthenticationToken(
                        userId, null, roles.map { SimpleGrantedAuthority(it) }
                    )
                    SecurityContextHolder.getContext().authentication = authentication
                } else {
                    println("Token is invalid")
                }
            } catch (e: Exception) {
                println("Error in JWT Authentication Filter: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}