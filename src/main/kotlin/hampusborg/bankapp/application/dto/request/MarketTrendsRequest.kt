package hampusborg.bankapp.application.dto.request

import jakarta.validation.constraints.NotEmpty

data class MarketTrendsRequest(
    @field:NotEmpty(message = "Symbol cannot be empty")
    val symbol: String
)