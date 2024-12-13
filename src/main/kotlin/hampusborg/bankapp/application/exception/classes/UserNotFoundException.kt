package hampusborg.bankapp.application.exception.classes

class UserNotFoundException(override val message: String) : RuntimeException(message)