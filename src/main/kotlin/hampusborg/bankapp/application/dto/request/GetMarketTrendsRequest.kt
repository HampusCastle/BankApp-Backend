package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.NotEmpty

data class GetMarketTrendsRequest(
    @field:NotEmpty(message = "Symbol cannot be empty")
    val symbol: String
)