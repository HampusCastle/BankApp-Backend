package hampusborg.bankapp.application.dto.request

data class FetchFinancialNewsRequest(
    val page: Int = 1,
    val pageSize: Int = 5,
    val category: String = "business"
)