package hampusborg.bankapp.application.controller

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
    fun registerUser(@Valid @RequestBody userRegistrationRequest: UserRegistrationRequest): ResponseEntity<UserRegistrationResponse> {
        return try {
            val response = authenticationService.registerUser(userRegistrationRequest)
            ResponseEntity.ok(response)
        } catch (e: DuplicateUserException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        } catch (e: AccountCreationException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody userLoginRequest: UserLoginRequest): ResponseEntity<String> {
        return try {
            val token = authenticationService.loginUser(userLoginRequest)
            ResponseEntity.ok(token)
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password")
        }
    }
}