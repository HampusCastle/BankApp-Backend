package hampusborg.bankapp.application.exception.classes

class AccountNotFoundException(override val message: String) : RuntimeException(message)