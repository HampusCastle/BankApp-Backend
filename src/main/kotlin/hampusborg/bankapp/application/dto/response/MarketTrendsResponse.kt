package hampusborg.bankapp.application.dto.response

data class MarketTrendsResponse(
    val trend: String,
    val price: String,
    val volume: String,
    val changePercent: String
)