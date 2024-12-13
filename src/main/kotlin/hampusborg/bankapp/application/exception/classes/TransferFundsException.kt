package hampusborg.bankapp.application.exception.classes

class TransferFundsException(override val message: String) : RuntimeException(message)