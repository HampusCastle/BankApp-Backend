package hampusborg.bankapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class BankAppApplication

fun main(args: Array<String>) {
    runApplication<BankAppApplication>(*args)
}
