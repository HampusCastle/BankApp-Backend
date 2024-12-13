package hampusborg.bankapp.application.exception.classes

class DuplicateUserException(override val message: String) : RuntimeException(message)