package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.FieldActivity
import br.com.triskin.domain.repository.FieldActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivitiesUseCase @Inject constructor(
    private val repository: FieldActivityRepository,
) {
    operator fun invoke(): Flow<List<FieldActivity>> = repository.getAll()
}
