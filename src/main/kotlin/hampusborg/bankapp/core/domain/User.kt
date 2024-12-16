package hampusborg.bankapp.core.domain

import hampusborg.bankapp.core.domain.enums.Role
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class User(
    @Id val id: String? = null,
    var email: String,
    var username: String,
    var password: String,
    var firstName: String,
    var lastName: String,
    val roles: List<Role> = listOf(Role.USER)
)