package hampusborg.bankapp.infrastructure.filter

import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class JwtAuthenticationFilter(private val jwtUtil: JwtUtil) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst("Authorization")
        val token = authHeader?.takeIf { it.startsWith("Bearer ") }?.substring(7)

        if (token != null) {
            return try {
                if (jwtUtil.isValidToken(token)) {
                    val (userId, roles) = jwtUtil.extractUserDetails(token)
                        ?: throw IllegalArgumentException("Invalid token details")

                    val authentication = UsernamePasswordAuthenticationToken(
                        userId, null, roles.map { SimpleGrantedAuthority(it) }
                    )
                    return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                } else {
                    Mono.error(IllegalArgumentException("Invalid token"))
                }
            } catch (e: Exception) {
                Mono.error(IllegalArgumentException("Error in JWT Authentication Filter: ${e.message}"))
            }
        }

        return chain.filter(exchange)
    }
}