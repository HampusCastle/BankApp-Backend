package hampusborg.bankapp.application.exception

import hampusborg.bankapp.application.exception.classes.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        log.warn("Validation error: {}", ex.message)
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST,
            error = "Validation Failed",
            message = "One or more fields have validation errors",
            errors = errors,
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, headers, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(
        SavingsGoalNotFoundException::class,
        AccountNotFoundException::class,
        UserNotFoundException::class,
        ScheduledPaymentNotFoundException::class
    )
    fun handleNotFoundExceptions(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        log.error("Resource not found: {}", ex.message)
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(
        InsufficientFundsException::class,
        InvalidTransferException::class,
        InvalidTransactionAmountException::class,
        InvalidPaymentScheduleException::class
    )
    fun handleBadRequestExceptions(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        log.warn("Bad request: {}", ex.message)
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.message)
    }

    @ExceptionHandler(DuplicateUserException::class)
    fun handleConflictException(ex: DuplicateUserException): ResponseEntity<ErrorResponse> {
        log.warn("Conflict: {}", ex.message)
        return buildErrorResponse(HttpStatus.CONFLICT, ex.message)
    }

    @ExceptionHandler(AccountNotActiveException::class)
    fun handleForbiddenException(ex: AccountNotActiveException): ResponseEntity<ErrorResponse> {
        log.error("Forbidden: {}", ex.message)
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.message)
    }

    @ExceptionHandler(InvalidAccountException::class)
    fun handleInvalidAccountException(ex: InvalidAccountException): ResponseEntity<ErrorResponse> {
        log.error("Invalid account: {}", ex.message)
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error: {}", ex.message)
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
    }

    private fun buildErrorResponse(status: HttpStatus, message: String?): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = status,
            error = status.reasonPhrase,
            message = message ?: "An error occurred",
            timestamp = System.currentTimeMillis()
        )
        return ResponseEntity(errorResponse, status)
    }
}