package hampusborg.bankapp.application.service.base
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import hampusborg.bankapp.application.dto.request.GenerateQRCodeRequest
import hampusborg.bankapp.application.dto.response.GeneratedQRCodeResponse
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*

@Service
class QRCodeService {

    fun generateQRCode(request: GenerateQRCodeRequest): GeneratedQRCodeResponse {
        val qrCodeString = "bankapp://pay?from=${request.fromUserId}&to=${request.toUserId}&amount=${request.amount}"

        val qrCodeImage = generateQRCodeImage(qrCodeString)

        val qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeImage)

        return GeneratedQRCodeResponse(
            qrCode = qrCodeBase64,
            qrCodeText = qrCodeString
        )
    }

    private fun generateQRCodeImage(qrCodeText: String): ByteArray {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 300, 300)

        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
        return outputStream.toByteArray()
    }
}