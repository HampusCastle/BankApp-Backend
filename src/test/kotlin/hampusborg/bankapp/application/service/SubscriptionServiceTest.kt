import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.service.SubscriptionService
import hampusborg.bankapp.core.domain.Subscription
import hampusborg.bankapp.core.repository.SubscriptionRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class SubscriptionServiceTest {

    private val repository: SubscriptionRepository = mock(SubscriptionRepository::class.java)
    private val service: SubscriptionService = SubscriptionService(repository)

    @Test
    fun `should create subscription`() {
        `when`(repository.save(any())).thenReturn(mock(Subscription::class.java))
        service.createSubscription(mock(SubscriptionRequest::class.java))
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should fetch subscriptions by status`() {
        `when`(repository.findAllByUserIdAndStatus("testUser", "active")).thenReturn(emptyList())
        val subscriptions = service.getSubscriptionsByStatus("testUser", "active")
        assertTrue(subscriptions.isEmpty())
    }
}