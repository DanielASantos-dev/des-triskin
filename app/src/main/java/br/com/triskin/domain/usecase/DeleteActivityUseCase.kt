package br.com.triskin.domain.usecase

import br.com.triskin.domain.repository.FieldActivityRepository
import javax.inject.Inject

class DeleteActivityUseCase @Inject constructor(
    private val repository: FieldActivityRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
