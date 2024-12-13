package hampusborg.bankapp.infrastructure.config

import hampusborg.bankapp.infrastructure.util.JwtUtil
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class JwtUtilTest {

    private lateinit var jwtUtil: JwtUtil

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private var expiration: Long = 0

    @BeforeEach
    fun setup() {
        jwtUtil = JwtUtil().apply {
            this.secret = this@JwtUtilTest.secret
            this.expiration = this@JwtUtilTest.expiration
        }
    }

    @Test
    fun `should generate and validate a token`() {
        val roles = listOf("ROLE_USER")
        val token = jwtUtil.generateToken("userId123", "testUser", roles)

        assertNotNull(token)
        assertTrue(jwtUtil.isValidToken(token))
    }

    @Test
    fun `should extract user details from token`() {
        val roles = listOf("ROLE_USER")
        val token = jwtUtil.generateToken("userId123", "testUser", roles)

        val userDetails = jwtUtil.extractUserDetails(token)
        assertNotNull(userDetails)
        assertEquals("userId123", userDetails!!.first)
        assertEquals(roles, userDetails.second)
    }

    @Test
    fun `should return false for expired token`() {
        val expiredJwtUtil = JwtUtil().apply {
            this.secret = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).toString()
            this.expiration = -1000
        }

        val token = expiredJwtUtil.generateToken("userId123", "testUser", listOf("ROLE_USER"))
        assertFalse(expiredJwtUtil.isValidToken(token))
    }
}
