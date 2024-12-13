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
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("Authorization")?.takeIf { it.startsWith("Bearer ") }?.substring(7)

        if (token != null) {
            try {
                if (jwtUtil.isValidToken(token)) {
                    val (userDetails, roles) = jwtUtil.extractUserDetails(token) ?: throw IllegalArgumentException("Invalid token details")

                    val authentication = UsernamePasswordAuthenticationToken(userDetails, null, roles.map { SimpleGrantedAuthority(it) })
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        filterChain.doFilter(request, response)
    }
}