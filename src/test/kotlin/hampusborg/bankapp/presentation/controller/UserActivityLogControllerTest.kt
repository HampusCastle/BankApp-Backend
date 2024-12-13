package hampusborg.bankapp.presentation.controller

import hampusborg.bankapp.application.service.UserActivityLogService
import hampusborg.bankapp.core.domain.UserActivityLog
import hampusborg.bankapp.infrastructure.util.JwtUtil
import hampusborg.bankapp.presentation.controller.UserActivityLogController
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserActivityLogController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserActivityLogControllerTest.UserActivityLogServiceTestConfig::class)
class UserActivityLogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userActivityLogService: UserActivityLogService

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @TestConfiguration
    class UserActivityLogServiceTestConfig {
        @Bean
        fun userActivityLogService(): UserActivityLogService = mock()

        @Bean
        fun jwtUtil(): JwtUtil = mock()
    }

    @Test
    fun `should fetch activity logs for the current user`() {
        val logs = listOf(
            UserActivityLog(id = "1", action = "Logged in", userId = "user123", timestamp = System.currentTimeMillis()),
            UserActivityLog(id = "2", action = "Transferred funds", userId = "user123", timestamp = System.currentTimeMillis())
        )

        whenever(userActivityLogService.getLogsByUserId("user123")).thenReturn(logs)
        whenever(jwtUtil.extractUserDetails("valid_token")).thenReturn(Pair("user123", listOf("ROLE_USER")))

        mockMvc.perform(
            get("/activity-logs/my-logs")
                .header("Authorization", "valid_token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[0].action").value("Logged in"))
            .andExpect(jsonPath("$[1].action").value("Transferred funds"))
    }

    @Test
    fun `should return bad request when user id is not extracted from token`() {
        whenever(jwtUtil.extractUserDetails("invalid_token")).thenReturn(null)

        mockMvc.perform(
            get("/activity-logs/my-logs")
                .header("Authorization", "invalid_token")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$").isEmpty())
    }
}