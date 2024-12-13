package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.QRCodeRequest
import hampusborg.bankapp.application.dto.response.QRCodeResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class QRCodeService {

    fun generateQRCode(request: QRCodeRequest): QRCodeResponse {
        val qrCodeString = "bankapp://pay?from=${request.fromUserId}&to=${request.toUserId}&amount=${request.amount}"
        val qrCodeEncoded = Base64.getEncoder().encodeToString(qrCodeString.toByteArray())
        return QRCodeResponse(qrCodeEncoded)
    }
}