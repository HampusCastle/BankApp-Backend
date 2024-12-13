package hampusborg.bankapp.application.mapper

import hampusborg.bankapp.application.dto.request.AccountRequest
import hampusborg.bankapp.application.dto.response.AccountResponse
import hampusborg.bankapp.core.domain.Account
import org.springframework.stereotype.Component

@Component
class AccountMapper {

    fun mapToAccount(dto: AccountRequest): Account {
        return Account(
            userId = dto.userId ?: throw IllegalArgumentException("UserId cannot be null"),
            accountType = dto.accountType,
            balance = dto.balance
        )
    }

    fun mapToAccountResponse(account: Account): AccountResponse {
        return AccountResponse(
            id = account.id ?: throw IllegalArgumentException("Account ID cannot be null"),
            name = account.accountType,
            balance = account.balance,
            accountType = account.accountType,
            userId = account.userId
        )
    }
}
