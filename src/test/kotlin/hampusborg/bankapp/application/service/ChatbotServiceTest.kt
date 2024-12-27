import hampusborg.bankapp.application.service.*
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class ChatbotServiceTest {

    private val chatbotService: ChatbotService = ChatbotService(
        accountService = mock(AccountService::class.java),
        transactionService = mock(TransactionService::class.java),
        marketTrendsService = mock(MarketTrendsService::class.java),
        scheduledPaymentService = mock(ScheduledPaymentService::class.java)
    )

    @Test
    fun `should return response for unrecognized query`() {
        val response = chatbotService.getChatbotResponse("random query", "testUser")
        assertNotNull(response)
    }

    @Test
    fun `should call getAccountBalances on balance query`() {
        chatbotService.getChatbotResponse("balance", "testUser")
        verify(mock(AccountService::class.java), times(1)).getAllAccountsByUser(anyString())
    }
}