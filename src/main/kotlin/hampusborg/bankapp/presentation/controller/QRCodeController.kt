package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.QRCodeRequest
import hampusborg.bankapp.application.dto.response.QRCodeResponse
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
        @Valid @RequestBody qrCodeRequest: QRCodeRequest
    ): ResponseEntity<QRCodeResponse> {
        val qrCodeResponse = qrCodeService.generateQRCode(qrCodeRequest)
        return ResponseEntity.ok(qrCodeResponse)
    }
}