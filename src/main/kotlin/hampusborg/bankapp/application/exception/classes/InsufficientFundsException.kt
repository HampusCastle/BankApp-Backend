package hampusborg.bankapp.application.exception.classes

class InsufficientFundsException(override val message: String) : RuntimeException(message)