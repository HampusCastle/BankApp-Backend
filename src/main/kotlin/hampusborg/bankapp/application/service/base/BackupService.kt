package hampusborg.bankapp.application.service.base

import com.fasterxml.jackson.databind.ObjectMapper
import hampusborg.bankapp.core.domain.User
import hampusborg.bankapp.core.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException

@Service
class BackupService(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val objectMapper: ObjectMapper
) {
    private val backupDirectory = "backups/"

    fun backupUserData() {
        val users = userRepository.findAll()

        try {
            val usersJson = objectMapper.writeValueAsString(users)
            val backupFile = File("$backupDirectory/users_backup.json")

            File(backupDirectory).mkdirs()

            backupFile.writeText(usersJson)
            println("User data backup completed successfully.")

        } catch (e: IOException) {
            println("Failed to create backup: ${e.message}")
        }
    }

    fun restoreUserData() {
        try {
            val backupFile = File("$backupDirectory/users_backup.json")

            if (backupFile.exists()) {
                val users: List<User> = objectMapper.readValue(backupFile, object : com.fasterxml.jackson.core.type.TypeReference<List<User>>() {})
                userRepository.deleteAll()
                userRepository.saveAll(users)
                println("User data restored successfully.")
            } else {
                println("Backup file not found!")
            }
        } catch (e: IOException) {
            println("Failed to restore data: ${e.message}")
        }
    }
}