package hampusborg.bankapp.infrastructure.config

import hampusborg.bankapp.core.domain.enums.Role
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class ApplicationStartup(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostConstruct
    fun seedDatabase() {
        if (userRepository.count() == 0L) {
            val adminUser = User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                email = "admin@admin.com",
                roles = listOf(Role.ADMIN),
                firstName = "Admin",
                lastName = "Admin",
            )
            userRepository.save(adminUser)

            val regularUsers = (1..9).map { index ->
                User(
                    username = "user$index",
                    password = passwordEncoder.encode("user$index"),
                    email = "user$index@example.com",
                    roles = listOf(Role.USER),
                    firstName = "User",
                    lastName = "User",
                )
            }
            userRepository.saveAll(regularUsers)
        }
    }
}