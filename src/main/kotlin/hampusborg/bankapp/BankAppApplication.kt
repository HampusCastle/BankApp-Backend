package hampusborg.bankapp

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class BankAppApplication

fun main(args: Array<String>) {

    val dotenv = Dotenv.load()

    System.setProperty("JWT_SECRET", dotenv["JWT_SECRET"] ?: "")
    System.setProperty("JWT_EXPIRATION", dotenv["JWT_EXPIRATION"]?.toLongOrNull()?.toString() ?: "3600000")
    System.setProperty("FINANCIAL_API_KEY", dotenv["FINANCIAL_API_KEY"] ?: "")
    System.setProperty("NEWSAPI_API_KEY", dotenv["NEWSAPI_API_KEY"] ?: "")

    System.setProperty("DATABASE_URL", dotenv["SPRING_DATA_MONGODB_URI"] ?: "mongodb://localhost:27017/BankApp")

    if (dotenv["MAIL_HOST"] != null) {
        System.setProperty("MAIL_HOST", dotenv["MAIL_HOST"] ?: "")
        System.setProperty("MAIL_PORT", dotenv["MAIL_PORT"] ?: "587")
        System.setProperty("MAIL_USERNAME", dotenv["MAIL_USERNAME"] ?: "")
        System.setProperty("MAIL_PASSWORD", dotenv["MAIL_PASSWORD"] ?: "")
    }

    runApplication<BankAppApplication>(*args)
}