package hampusborg.bankapp

import io.github.cdimascio.dotenv.Dotenv
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
@OpenAPIDefinition(
    info = Info(
        title = "BankApp API",
        description = "API Documentation for BankApp",
        version = "1.0"
    )
)
class BankAppApplication

fun main(args: Array<String>) {
    val dotenv = Dotenv.load()

    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    runApplication<BankAppApplication>(*args)
}