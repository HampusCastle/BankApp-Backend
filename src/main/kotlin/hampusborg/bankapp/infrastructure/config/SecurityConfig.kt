package hampusborg.bankapp.infrastructure.config

import hampusborg.bankapp.infrastructure.filter.JwtAuthenticationFilter
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtUtil: JwtUtil) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
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
            .authorizeHttpRequests { authRequest ->
                authRequest
                    .requestMatchers("/auth/register", "/auth/login").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
