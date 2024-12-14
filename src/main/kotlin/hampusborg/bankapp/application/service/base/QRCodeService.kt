package hampusborg.bankapp.application.service.base

import hampusborg.bankapp.application.dto.request.GenerateQRCodeRequest
import hampusborg.bankapp.application.dto.response.GeneratedQRCodeResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class QRCodeService {

    fun generateQRCode(request: GenerateQRCodeRequest): GeneratedQRCodeResponse {
        val qrCodeString = "bankapp://pay?from=${request.fromUserId}&to=${request.toUserId}&amount=${request.amount}"
        val qrCodeEncoded = Base64.getEncoder().encodeToString(qrCodeString.toByteArray())
        return GeneratedQRCodeResponse(qrCodeEncoded)
    }
}