import hampusborg.bankapp.application.service.base.BackupService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BackupScheduler(private val backupService: BackupService) {

    @Scheduled(cron = "0 0 0 * * *")
    fun scheduleBackup() {
        println("Running scheduled backup...")
        backupService.backupUserData()
    }
}