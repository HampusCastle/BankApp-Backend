package hampusborg.bankapp.core.domain

enum class Role(val authority: String) {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN")
}