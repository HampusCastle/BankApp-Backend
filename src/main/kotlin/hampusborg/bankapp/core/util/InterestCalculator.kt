package hampusborg.bankapp.core.util

object InterestCalculator {
    fun calculateInterest(balance: Double, interestRate: Double?): Double {
        return if (interestRate != null) {
            balance * (interestRate / 100)
        } else {
            0.0
        }
    }
}