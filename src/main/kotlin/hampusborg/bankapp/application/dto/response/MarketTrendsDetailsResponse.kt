package hampusborg.bankapp.application.dto.response

data class MarketTrendsDetailsResponse(
    val trend: String,
    val price: String,
    val changePercent: String
)