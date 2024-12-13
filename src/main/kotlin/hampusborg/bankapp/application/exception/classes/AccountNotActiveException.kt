package hampusborg.bankapp.application.exception.classes

class AccountNotActiveException(override val message: String) : RuntimeException(message)