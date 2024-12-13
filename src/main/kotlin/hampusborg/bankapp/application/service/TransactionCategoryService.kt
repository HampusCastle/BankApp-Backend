package hampusborg.bankapp.application.service

import hampusborg.bankapp.core.domain.TransactionCategory
import hampusborg.bankapp.core.repository.TransactionCategoryRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class TransactionCategoryService(
    private val transactionCategoryRepository: TransactionCategoryRepository
) {

    fun createCategory(name: String, description: String?): TransactionCategory {
        val category = TransactionCategory(name = name, description = description)
        return transactionCategoryRepository.save(category)
    }

    fun getAllCategories(): List<TransactionCategory> {
        return transactionCategoryRepository.findAll()
    }
}