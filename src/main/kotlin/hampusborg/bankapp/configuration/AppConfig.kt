package hampusborg.bankapp.configuration

import org.springframework.beans.factory.annotation.Value

class AppConfig {

    @Value("\${MAIL_HOST}")
    lateinit var mailHost: String

    @Value("\${MAIL_PORT}")
    var mailPort: Int = 0

    @Value("\${MAIL_USERNAME}")
    lateinit var mailUsername: String

    @Value("\${MAIL_PASSWORD}")
    lateinit var mailPassword: String
}