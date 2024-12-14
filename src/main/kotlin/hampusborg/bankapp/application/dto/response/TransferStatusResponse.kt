package hampusborg.bankapp.application.dto.response

data class TransferStatusResponse(
    val message: String,
    val status: String = "success"
)