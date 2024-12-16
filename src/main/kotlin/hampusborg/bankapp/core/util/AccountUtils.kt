package hampusborg.bankapp.core.util

import hampusborg.bankapp.application.dto.response.AccountDetailsResponse
import hampusborg.bankapp.core.domain.Account

object AccountUtils {
    fun mapToAccountDetailsResponse(account: Account): AccountDetailsResponse {
        return AccountDetailsResponse(
            id = account.id!!,
            name = account.name,
            balance = account.balance,
            accountType = account.accountType.name,
            userId = account.userId
        )
    }
}
