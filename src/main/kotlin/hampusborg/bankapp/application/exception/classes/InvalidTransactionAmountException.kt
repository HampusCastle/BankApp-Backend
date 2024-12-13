package hampusborg.bankapp.application.exception.classes

class InvalidTransactionAmountException(override val message: String) : RuntimeException(message)