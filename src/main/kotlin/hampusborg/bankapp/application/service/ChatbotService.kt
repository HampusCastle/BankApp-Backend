/*
package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.GetMarketTrendsRequest
import hampusborg.bankapp.application.dto.response.ChatbotResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChatbotService(
    private val accountService: AccountService,
    private val transactionService: TransactionService,
    private val marketTrendsService: MarketTrendsService,
    private val scheduledPaymentService: ScheduledPaymentService
) {

    //TODO - Funkar i postman, men hann inte implementera den i frontend, så borttagen.

    private val log = LoggerFactory.getLogger(ChatbotService::class.java)

    fun getChatbotResponse(query: String, userId: String): ChatbotResponse {
        return try {
            when {
                query.contains("balance", ignoreCase = true) -> getAccountBalances(userId)
                query.contains("transactions", ignoreCase = true) -> getTransactionHistory(userId)
                query.contains("scheduled payments", ignoreCase = true) -> getScheduledPayments(userId)
                query.contains("market trends", ignoreCase = true) -> getMarketTrends(query)
                else -> {
                    log.warn("Unrecognized query '$query' for user '$userId'")
                    ChatbotResponse("Help", "I didn't understand your query. Please try again.")
                }
            }
        } catch (e: Exception) {
            log.error("Error processing chatbot query '$query' for user '$userId': ${e.message}", e)
            ChatbotResponse("Error", "An unexpected error occurred while processing your query.")
        }
    }

    private fun getAccountBalances(userId: String): ChatbotResponse {
        val accounts = accountService.getAllAccountsByUser(userId)
        if (accounts.isEmpty()) log.info("No accounts found for user: $userId")

        val response = accounts.joinToString("\n") {
            "Account ID: ${it.id}, Balance: ${it.balance}, Type: ${it.accountType}"
        }
        return ChatbotResponse("Account Balances", response.ifEmpty { "No accounts found." })
    }

    private fun getTransactionHistory(userId: String): ChatbotResponse {
        val transactions = transactionService.getTransactionsByUser(userId)
        if (transactions.isEmpty()) log.info("No transactions found for user: $userId")

        val response = transactions.take(10).joinToString("\n") {
            "Transaction ID: ${it.id}, Amount: ${it.amount}, Date: ${it.date}"
        } + if (transactions.size > 10) "\n...and more" else ""

        return ChatbotResponse("Transaction History", response.ifEmpty { "No transactions found." })
    }

    private fun getScheduledPayments(userId: String): ChatbotResponse {
        val payments = scheduledPaymentService.getScheduledPaymentsByUser(userId)
        if (payments.isEmpty()) log.info("No scheduled payments found for user: $userId")

        val response = payments.joinToString("\n") {
            "Payment ID: ${it.id}, Amount: ${it.amount}, Next Payment Date: ${it.nextPaymentDate}"
        }
        return ChatbotResponse("Scheduled Payments", response.ifEmpty { "No scheduled payments found." })
    }

    private fun getMarketTrends(query: String): ChatbotResponse {
        val symbols = listOf("AAPL", "IBM", "GOOG", "MSFT", "AMZN")
        val symbol = symbols.firstOrNull { query.contains(it, ignoreCase = true) } ?: "AAPL"

        val trends = marketTrendsService.getMarketTrends(GetMarketTrendsRequest(symbol = symbol))
        val response = """
        Symbol: $symbol (defaulted to AAPL if unspecified)
        Market Trend: ${trends.trend}
        Current Price: ${trends.price} SEK
        Change: ${trends.changePercent} %
        """.trimIndent()

        return ChatbotResponse("Market Trends for $symbol", response)
    }
}
*/
