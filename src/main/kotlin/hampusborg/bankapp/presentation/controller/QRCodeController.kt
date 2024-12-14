package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.GenerateQRCodeRequest
import hampusborg.bankapp.application.dto.response.GeneratedQRCodeResponse
import hampusborg.bankapp.application.service.QRCodeService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/qrcode")
class QRCodeController(private val qrCodeService: QRCodeService) {

    @PostMapping("/generate")
    fun generateQRCode(
        @Valid @RequestBody generateQrCodeRequest: GenerateQRCodeRequest
    ): ResponseEntity<GeneratedQRCodeResponse> {
        val qrCodeResponse = qrCodeService.generateQRCode(generateQrCodeRequest)
        return ResponseEntity.ok(qrCodeResponse)
    }
}