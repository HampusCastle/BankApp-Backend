package hampusborg.bankapp.core.domain.enums

enum class Role(val authority: String) {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN")
}