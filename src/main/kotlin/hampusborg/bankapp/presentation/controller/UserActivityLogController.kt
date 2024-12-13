package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.service.UserActivityLogService
import hampusborg.bankapp.core.domain.UserActivityLog
import hampusborg.bankapp.infrastructure.util.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/activity-logs")
class UserActivityLogController(
    private val userActivityLogService: UserActivityLogService,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/my-logs")
    fun getMyActivityLogs(@RequestHeader("Authorization") token: String): ResponseEntity<List<UserActivityLog>> {
        val userId = jwtUtil.extractUserDetails(token.substringAfter(" "))?.first
        return if (userId != null) {
            val logs = userActivityLogService.getLogsByUserId(userId)
            ResponseEntity.ok(logs)
        } else {
            ResponseEntity.badRequest().body(emptyList())
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    fun getUserActivityLogs(@PathVariable userId: String): ResponseEntity<List<UserActivityLog>> {
        val logs = userActivityLogService.getLogsByUserId(userId)
        return if (logs.isNotEmpty()) {
            ResponseEntity.ok(logs)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    fun getAllActivityLogs(): ResponseEntity<List<UserActivityLog>> {
        val logs = userActivityLogService.getAllLogs()
        return ResponseEntity.ok(logs)
    }
}