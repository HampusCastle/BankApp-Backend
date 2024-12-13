package hampusborg.bankapp.application.exception.classes

class InvalidTransferException(override val message: String) : RuntimeException(message)