package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.dto.request.AuthenticateUserRequest
import hampusborg.bankapp.application.dto.request.RegisterUserRequest
import hampusborg.bankapp.application.dto.response.AuthenticateUserResponse
import hampusborg.bankapp.application.dto.response.RegisteredUserResponse
import hampusborg.bankapp.application.service.AuthenticationService
import hampusborg.bankapp.application.exception.classes.DuplicateUserException
import hampusborg.bankapp.application.exception.classes.AccountCreationException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(RegisteredUserResponse(username = "", roles = listOf(), message = "Username already exists, please choose another one"))
        } catch (e: AccountCreationException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RegisteredUserResponse(username = "", roles = listOf(), message = "Account creation failed, please try again"))
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