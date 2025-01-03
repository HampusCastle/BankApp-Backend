package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.SubscriptionRequest
import hampusborg.bankapp.application.service.base.CacheHelperService
import hampusborg.bankapp.core.domain.Subscription
import hampusborg.bankapp.core.repository.SubscriptionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SubscriptionServiceTest {

    private val repository: SubscriptionRepository = mock()
    private val cacheHelperService: CacheHelperService = mock()
    private val service = SubscriptionService(repository, cacheHelperService)

    @Test
    fun `should create subscription successfully`() {
        val request = SubscriptionRequest(
            amount = 20.0,
            serviceName = "Netflix",
            interval = "monthly",
            categoryId = "SUBSCRIPTIONS",
            fromAccountId = "12345",
            toAccountId = "67890"
        )
        val subscription = Subscription(
            id = "1",
            userId = "testUser",
            amount = 20.0,
            serviceName = "Netflix",
            interval = "monthly",
            categoryId = "SUBSCRIPTIONS",
            nextPaymentDate = System.currentTimeMillis(),
            status = "active",
            fromAccountId = "12345",
            toAccountId = "67890"
        )

        whenever(repository.save(any())).thenReturn(subscription)

        val result = service.createSubscription(request)

        assertEquals("1", result.id)
        assertEquals("Netflix", result.serviceName)
        verify(repository, times(1)).save(any())
    }

    @Test
    fun `should get subscriptions by status`() {
        val subscription = Subscription(
            id = "1",
            userId = "testUser",
            amount = 20.0,
            serviceName = "Netflix",
            interval = "monthly",
            categoryId = "SUBSCRIPTIONS",
            nextPaymentDate = System.currentTimeMillis(),
            status = "active",
            fromAccountId = "12345",
            toAccountId = "67890"
        )

        whenever(repository.findAllByUserIdAndStatus("testUser", "active")).thenReturn(listOf(subscription))

        val result = service.getSubscriptionsByStatus("testUser", "active")

        assertEquals(1, result.size)
        assertEquals("Netflix", result[0].serviceName)
        verify(repository, times(1)).findAllByUserIdAndStatus("testUser", "active")
    }
}