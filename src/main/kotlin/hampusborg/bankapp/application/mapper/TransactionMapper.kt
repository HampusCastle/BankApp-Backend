package hampusborg.bankapp.application.mapper

import hampusborg.bankapp.application.dto.request.TransactionRequest
import hampusborg.bankapp.application.dto.response.TransactionResponse
import hampusborg.bankapp.core.domain.Transaction
import org.springframework.stereotype.Component

@Component
class TransactionMapper {

    fun mapToTransaction(dto: TransactionRequest): Transaction {
        return Transaction(
            fromAccountId = dto.fromAccountId,
            toAccountId = dto.toAccountId,
            amount = dto.amount,
            timestamp = System.currentTimeMillis(),
            categoryId = dto.categoryId,
            userId = "user123",
            date = "2024-12-11"
        )
    }

    fun mapToTransactionResponse(transaction: Transaction): TransactionResponse {
        return TransactionResponse(
            fromAccountId = transaction.fromAccountId,
            toAccountId = transaction.toAccountId,
            amount = transaction.amount,
            timestamp = transaction.timestamp,
            categoryId = transaction.categoryId
        )
    }
}