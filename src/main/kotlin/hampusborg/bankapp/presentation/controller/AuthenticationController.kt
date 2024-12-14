package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.AuthenticateUserRequest
import hampusborg.bankapp.application.dto.request.RegisterUserRequest
import hampusborg.bankapp.application.dto.response.AuthenticateUserResponse
import hampusborg.bankapp.application.dto.response.RegisteredUserResponse
import hampusborg.bankapp.application.exception.classes.AccountCreationException
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.application.service.AuthenticationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {

    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody registerUserRequest: RegisterUserRequest): ResponseEntity<RegisteredUserResponse> {
        return try {
            val response = authenticationService.registerUser(registerUserRequest)
            ResponseEntity.ok(response)
        } catch (e: DuplicateUserException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        } catch (e: AccountCreationException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody authenticateUserRequest: AuthenticateUserRequest): ResponseEntity<AuthenticateUserResponse> {
        return try {
            val token = authenticationService.loginUser(authenticateUserRequest)

            val response = AuthenticateUserResponse(token)

            ResponseEntity.ok(response)
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthenticateUserResponse("Invalid username or password"))
        }
    }
}