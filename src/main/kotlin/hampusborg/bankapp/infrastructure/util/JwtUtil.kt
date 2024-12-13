package hampusborg.bankapp.infrastructure.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Claims
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    lateinit var secret: String

    @Value("\${jwt.expiration}")
    var expiration: Long = 3600000

    private fun getSigningKey(): Key {
        return if (secret.length >= 32) {
            Keys.hmacShaKeyFor(secret.toByteArray())
        } else {
            SecretKeySpec(secret.toByteArray(), SignatureAlgorithm.HS256.jcaName)
        }
    }

    fun generateToken(userId: String, username: String, roles: List<String>): String {
        val claims = Jwts.claims().setSubject(username)
        claims["userId"] = userId
        claims["roles"] = roles
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact()
    }

    fun extractUserDetails(token: String): Pair<String?, List<String>>? {
        return try {
            val claims = extractAllClaims(token)
            val roles = (claims["roles"] as? List<*>)?.map { it.toString() } ?: emptyList()
            val userId = claims["userId"] as? String
            Pair(userId, roles)
        } catch (e: Exception) {
            println("Error extracting user details: ${e.message}")
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    fun isValidToken(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                println("Invalid token format")
                return false
            }
            !isTokenExpired(token)
        } catch (e: Exception) {
            println("Error in token validation: ${e.message}")
            false
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }
}