package hampusborg.bankapp.application.dto.request

import hampusborg.bankapp.core.domain.enums.TransactionCategory
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class TransactionFilterRequest(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val fromDate: LocalDate? = null,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val toDate: LocalDate? = null,

    val category: TransactionCategory? = null,

    val minAmount: Double? = null,

    val maxAmount: Double? = null
)