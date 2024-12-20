package hampusborg.bankapp.application.exception

import hampusborg.bankapp.application.exception.classes.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import java.time.Instant

@ControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(ex: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Validation Failed",
            message = "One or more fields have validation errors",
            errors = errors,
            timestamp = Instant.now().toEpochMilli()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(
        SavingsGoalNotFoundException::class,
        AccountNotFoundException::class,
        UserNotFoundException::class,
        ScheduledPaymentNotFoundException::class
    )
    fun handleNotFoundExceptions(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(
        InsufficientFundsException::class,
        InvalidTransferException::class,
        InvalidTransactionAmountException::class,
        InvalidPaymentScheduleException::class
    )
    fun handleBadRequestExceptions(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.message)
    }

    @ExceptionHandler(DuplicateUserException::class)
    fun handleConflictException(ex: DuplicateUserException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.message)
    }

    @ExceptionHandler(AccountNotActiveException::class)
    fun handleForbiddenException(ex: AccountNotActiveException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.message)
    }

    @ExceptionHandler(InvalidAccountException::class)
    fun handleInvalidAccountException(ex: InvalidAccountException): ResponseEntity<ErrorResponse> {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("An unexpected error occurred: ", ex)
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
    }

    private fun buildErrorResponse(status: HttpStatus, message: String?): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = status,
            error = status.reasonPhrase,
            message = message ?: "An error occurred",
            timestamp = Instant.now().toEpochMilli()
        )
        return ResponseEntity(errorResponse, status)
    }
}