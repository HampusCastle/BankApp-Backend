package hampusborg.bankapp.application.service

import hampusborg.bankapp.application.dto.request.TransferRequest
import hampusborg.bankapp.core.domain.Account
import hampusborg.bankapp.core.repository.AccountRepository
import hampusborg.bankapp.core.repository.TransactionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class TransferServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val transactionRepository: TransactionRepository = mock()
    private val notificationService: NotificationService = mock()
    private val userActivityLogService: UserActivityLogService = mock()

    private val transferService = TransferService(
        accountRepository, transactionRepository, notificationService, userActivityLogService
    )

    @Test
    fun `should transfer funds between accounts successfully`() {
        val transferRequest = TransferRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 100.0,
            categoryId = "category1"
        )
        val userId = "user1"

        val fromAccount = Account(id = "account1", userId = "user1", accountType = "Checking", balance = 200.0)
        val toAccount = Account(id = "account2", userId = "user1", accountType = "Checking", balance = 100.0)

        whenever(accountRepository.findById(transferRequest.fromAccountId)).thenReturn(Optional.of(fromAccount))
        whenever(accountRepository.findById(transferRequest.toAccountId)).thenReturn(Optional.of(toAccount))

        val transferResponse = transferService.transferFunds(transferRequest, userId)

        assertEquals("Transfer successful", transferResponse.message)

        assertEquals(100.0, fromAccount.balance)
        assertEquals(200.0, toAccount.balance)

        verify(notificationService).createNotification(any())
        verify(userActivityLogService).logActivity(any(), any(), any())
        verify(accountRepository).save(fromAccount)
        verify(accountRepository).save(toAccount)
        verify(transactionRepository).save(any())
    }

    @Test
    fun `should throw exception for insufficient balance`() {
        val transferRequest = TransferRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 300.0,
            categoryId = "category1"
        )
        val userId = "user1"

        val fromAccount = Account(id = "account1", userId = "user1", accountType = "Checking", balance = 200.0)
        val toAccount = Account(id = "account2", userId = "user1", accountType = "Checking", balance = 100.0)

        whenever(accountRepository.findById(transferRequest.fromAccountId)).thenReturn(Optional.of(fromAccount))
        whenever(accountRepository.findById(transferRequest.toAccountId)).thenReturn(Optional.of(toAccount))

        val exception = assertThrows<RuntimeException> {
            transferService.transferFunds(transferRequest, userId)
        }

        assertEquals("Insufficient balance.", exception.message)
        verify(accountRepository, never()).save(any())
        verify(transactionRepository, never()).save(any())
    }

    @Test
    fun `should throw exception when fromAccount not found`() {
        val transferRequest = TransferRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 100.0,
            categoryId = "category1"
        )
        val userId = "user1"

        whenever(accountRepository.findById(transferRequest.fromAccountId)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            transferService.transferFunds(transferRequest, userId)
        }

        assertEquals("From account not found.", exception.message)
        verify(accountRepository, never()).save(any())
        verify(transactionRepository, never()).save(any())
    }

    @Test
    fun `should throw exception when toAccount not found`() {
        val transferRequest = TransferRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 100.0,
            categoryId = "category1"
        )
        val userId = "user1"

        val fromAccount = Account(id = "account1", userId = "user1", accountType = "Checking", balance = 200.0)

        whenever(accountRepository.findById(transferRequest.fromAccountId)).thenReturn(Optional.of(fromAccount))
        whenever(accountRepository.findById(transferRequest.toAccountId)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            transferService.transferFunds(transferRequest, userId)
        }

        assertEquals("To account not found.", exception.message)
        verify(accountRepository, never()).save(any())
        verify(transactionRepository, never()).save(any())
    }

    @Test
    fun `should throw exception for invalid user ID`() {
        val transferRequest = TransferRequest(
            fromAccountId = "account1",
            toAccountId = "account2",
            amount = 100.0,
            categoryId = "category1"
        )
        val userId = "user2"

        val fromAccount = Account(id = "account1", userId = "user1", accountType = "Checking", balance = 200.0)
        val toAccount = Account(id = "account2", userId = "user1", accountType = "Checking", balance = 100.0)

        whenever(accountRepository.findById(transferRequest.fromAccountId)).thenReturn(Optional.of(fromAccount))
        whenever(accountRepository.findById(transferRequest.toAccountId)).thenReturn(Optional.of(toAccount))

        val exception = assertThrows<RuntimeException> {
            transferService.transferFunds(transferRequest, userId)
        }

        assertEquals("Transfer failed: invalid accounts.", exception.message)
        verify(accountRepository, never()).save(any())
        verify(transactionRepository, never()).save(any())
    }
}
