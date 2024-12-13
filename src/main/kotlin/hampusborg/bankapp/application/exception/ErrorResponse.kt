package hampusborg.bankapp.application.exception

import org.springframework.http.HttpStatus

data class ErrorResponse(
    val status: HttpStatus,
    val error: String,
    val message: String?,
    val errors: Map<String, String>? = null,
    val timestamp: Long = System.currentTimeMillis()
)