package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.FetchFinancialNewsRequest
import hampusborg.bankapp.application.dto.request.GetMarketTrendsRequest
import hampusborg.bankapp.application.dto.response.MarketTrendsDetailsResponse
import hampusborg.bankapp.application.dto.response.ScheduledPaymentDetailsResponse
import hampusborg.bankapp.application.dto.response.TransactionHistoryDetailsResponse
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.ScheduledPaymentRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChatbotService(
    private val transactionRepository: TransactionRepository,
    private val scheduledPaymentRepository: ScheduledPaymentRepository,
    private val accountRepository: AccountRepository,
    private val marketTrendsService: MarketTrendsService,
    private val financialNewsService: FinancialNewsService
) {

    private val logger = LoggerFactory.getLogger(ChatbotService::class.java)

    fun getTransactionHistory(userId: String): TransactionHistoryDetailsResponse {
        logger.info("Fetching transaction history for user: $userId")
        val transactions = transactionRepository.findByFromAccountId(userId) + transactionRepository.findByToAccountId(userId)
        val history = transactions.joinToString("\n") { "Från: ${it.fromAccountId}, Till: ${it.toAccountId}, Belopp: ${it.amount} SEK" }
        logger.info("Transaction history for user $userId: $history")
        return TransactionHistoryDetailsResponse(history.ifEmpty { "Du har inga överföringar ännu." })
    }

    fun getMarketTrends(userId: String): MarketTrendsDetailsResponse {
        logger.info("Fetching market trends for user: $userId")

        val getMarketTrendsRequest = GetMarketTrendsRequest(symbol = "AAPL")
        val marketTrends = marketTrendsService.getMarketTrends(getMarketTrendsRequest)

        return MarketTrendsDetailsResponse(
            trend = marketTrends.trend,
            price = marketTrends.price,
            volume = marketTrends.volume,
            changePercent = marketTrends.changePercent
        )
    }

    fun getScheduledPayments(userId: String): ScheduledPaymentDetailsResponse {
        logger.info("Fetching scheduled payments for user: $userId")
        val payments = scheduledPaymentRepository.findByUserId(userId)
        val scheduledPayments = payments.joinToString("\n") { "Betalning till: ${it.toAccountId}, Belopp: ${it.amount}, Nästa betalning: ${it.nextPaymentDate}" }
        return ScheduledPaymentDetailsResponse(scheduledPayments.ifEmpty { "Du har inga planerade betalningar." })
    }

    fun getAccountInfo(userId: String): String {
        logger.info("Fetching account info for user: $userId")
        val accounts = accountRepository.findByUserId(userId)
        val accountInfo = accounts.joinToString("\n") { "Konto-ID: ${it.id}, Typ: ${it.accountType}, Saldo: ${it.balance} SEK" }
        return accountInfo.ifEmpty { "Du har inga konton än." }
    }

    fun getFinancialNews(): String {
        logger.info("Fetching financial news")
        val newsRequest = FetchFinancialNewsRequest(page = 1, pageSize = 5, category = "business")
        val news = financialNewsService.getFinancialNews(newsRequest)
        return news.joinToString("\n") {
            "Titel: ${it.title}\nBeskrivning: ${it.description}\nKälla: ${it.source}\nLäs mer: ${it.url}"
        }
    }

    fun getChatbotResponse(query: String, userId: String): String {
        logger.info("Received query: $query from user: $userId")
        return when {
            query.contains("saldo", ignoreCase = true) -> getAccountInfo(userId)
            query.contains("transaktioner", ignoreCase = true) -> getTransactionHistory(userId).history
            query.contains("konto", ignoreCase = true) -> getAccountInfo(userId)
            query.contains("planerade betalningar", ignoreCase = true) -> getScheduledPayments(userId).message
            query.contains("trender", ignoreCase = true) -> getMarketTrends(userId).let {
                "Market Trend: ${it.trend}\nCurrent Price: ${it.price} SEK\nVolume: ${it.volume}\nChange: ${it.changePercent} %"
            }
            query.contains("nyheter", ignoreCase = true) -> getFinancialNews()
            else -> "Jag förstår inte din fråga. Försök att ställa en annan fråga."
        }
    }
}