package hampusborg.bankapp.application.dto.response

data class TransferResponse(
    val message: String,
    val status: String = "success"
)