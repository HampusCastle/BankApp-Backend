package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.response.MarketTrendsDetailsResponse
import hampusborg.bankapp.application.dto.response.ScheduledPaymentDetailsResponse
import hampusborg.bankapp.application.dto.response.TransactionHistoryDetailsResponse
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.application.service.base.ExternalApiNewsHandler
import org.springframework.stereotype.Service

@Service
class ChatbotService(
    private val cacheHelperService: CacheHelperService
) {

    fun getTransactionHistory(userId: String): TransactionHistoryDetailsResponse {


        return cacheHelperService.getTransactionHistory(userId)
    }

    fun getMarketTrends(userId: String): MarketTrendsDetailsResponse {


        return cacheHelperService.getMarketTrends(userId)
    }

    fun getScheduledPayments(userId: String): ScheduledPaymentDetailsResponse {


        return cacheHelperService.getScheduledPayments(userId)
    }

    fun getAccountInfo(userId: String): String {


        return cacheHelperService.getAccountInfo(userId)
    }

    fun getFinancialNews(): List<ExternalApiNewsHandler.FinancialNewsDetailsResponse> {
        return cacheHelperService.getFinancialNews()  // Use cacheHelperService to get financial news
    }

    fun getChatbotResponse(query: String, userId: String): String {


        return when {
            query.contains("saldo", ignoreCase = true) -> getAccountInfo(userId)
            query.contains("transaktioner", ignoreCase = true) -> getTransactionHistory(userId).history
            query.contains("konto", ignoreCase = true) -> getAccountInfo(userId)
            query.contains("planerade betalningar", ignoreCase = true) -> getScheduledPayments(userId).message
            query.contains("trender", ignoreCase = true) -> getMarketTrends(userId).let {
                "Market Trend: ${it.trend}\nCurrent Price: ${it.price} SEK\nVolume: ${it.volume}\nChange: ${it.changePercent} %"
            }
            query.contains("nyheter", ignoreCase = true) -> getFinancialNews().joinToString("\n") {
                "Title: ${it.title}\nDescription: ${it.description}\nSource: ${it.source}\nURL: ${it.url}\n"
            }
            else -> "Jag förstår inte din fråga. Försök att ställa en annan fråga."
        }
    }
}