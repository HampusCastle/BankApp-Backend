package hampusborg.bankapp.infrastructure.config

import hampusborg.bankapp.infrastructure.filter.JwtAuthenticationFilter
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
class SecurityConfig(private val jwtUtil: JwtUtil) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.csrf { it.disable() }
            .cors { cors ->
                cors.configurationSource {
                    CorsConfiguration().apply {
                        allowedOrigins = listOf("http://localhost:5173", "http://localhost:8080")
                        allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With", "*")
                        allowCredentials = true
                    }
                }
            }
            .authorizeExchange { authRequest ->
                authRequest
                    .pathMatchers("/auth/register", "/auth/login").permitAll()
                    .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .pathMatchers("/users/**").authenticated()
                    .anyExchange().authenticated()
            }

        http.addFilterAt(JwtAuthenticationFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)

        return http.build()
    }
}