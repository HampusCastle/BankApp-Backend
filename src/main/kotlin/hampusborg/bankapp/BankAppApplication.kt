package hampusborg.bankapp

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableCaching
@EnableWebFlux
class BankAppApplication

fun main(args: Array<String>) {
    val dotenv = Dotenv.load()

    dotenv.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }

    runApplication<BankAppApplication>(*args)
}