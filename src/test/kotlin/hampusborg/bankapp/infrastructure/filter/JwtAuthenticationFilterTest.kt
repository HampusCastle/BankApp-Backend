package hampusborg.bankapp.infrastructure.filter

import hampusborg.bankapp.infrastructure.util.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthenticationFilterTest {

    private val jwtUtil = mock(JwtUtil::class.java)
    private val filter = JwtAuthenticationFilter(jwtUtil)

    @Test
    fun `should authenticate valid JWT token`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Bearer validToken")
        `when`(jwtUtil.isValidToken("validToken")).thenReturn(true)
        `when`(jwtUtil.extractUserDetails("validToken")).thenReturn(Pair("userId123", listOf("ROLE_USER")))

        filter.doFilterInternal(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assert(authentication != null)
        assert(authentication!!.principal == "userId123")
        assert(authentication.authorities.any { it.authority == "ROLE_USER" })

        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should not authenticate invalid JWT token`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val filterChain = mock(FilterChain::class.java)

        `when`(request.getHeader("Authorization")).thenReturn("Bearer invalidToken")
        `when`(jwtUtil.isValidToken("invalidToken")).thenReturn(false)

        filter.doFilterInternal(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assert(authentication == null)

        verify(filterChain).doFilter(request, response)
    }
}
