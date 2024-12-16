package hampusborg.bankapp.application.dto.response

data class GeneratedQRCodeResponse(
    val qrCode: String,
    val qrCodeText: String? = null
)