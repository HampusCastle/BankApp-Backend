package hampusborg.bankapp.application.exception.classes


class InvalidAccountException(override val message: String) : RuntimeException(message)