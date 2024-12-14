package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.InitiateTransferRequest
import hampusborg.bankapp.application.service.base.PaymentGatewayService
import hampusborg.bankapp.application.service.base.PaymentService
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*
import kotlin.test.assertEquals

class PaymentServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val transactionRepository: TransactionRepository = mock()
    private val paymentGatewayService: PaymentGatewayService = mock()

    private val paymentService = PaymentService(
        accountRepository,
        transactionRepository,
        paymentGatewayService
    )

    @Test
    fun `should process payment successfully`() {
        val initiateTransferRequest = InitiateTransferRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 100.0,
            categoryId = "category1"
        )
        val userId = "user1"

        val fromAccount = mock<Account>()
        val toAccount = mock<Account>()

        whenever(fromAccount.userId).thenReturn(userId)
        whenever(toAccount.userId).thenReturn(userId)
        whenever(fromAccount.balance).thenReturn(200.0)
        whenever(toAccount.balance).thenReturn(100.0)

        whenever(accountRepository.findById(initiateTransferRequest.fromAccountId)).thenReturn(Optional.of(fromAccount))
        whenever(accountRepository.findById(initiateTransferRequest.toAccountId)).thenReturn(Optional.of(toAccount))

        whenever(paymentGatewayService.processPayment(any(), any(), any())).thenReturn(true)

        val transaction = paymentService.handleTransfer(initiateTransferRequest, userId)

        assertEquals("account1", transaction.fromAccountId)
        assertEquals("account2", transaction.toAccountId)
        assertEquals(100.0, transaction.amount)
    }
}