package br.com.triskin.data.repository

import br.com.triskin.data.local.dao.FieldActivityDao
import br.com.triskin.data.mapper.FieldActivityCodec
import br.com.triskin.domain.model.FieldActivity
import br.com.triskin.domain.repository.FieldActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldActivityRepositoryImpl @Inject constructor(
    private val dao: FieldActivityDao,
    private val codec: FieldActivityCodec,
) : FieldActivityRepository {

    override fun getAll(): Flow<List<FieldActivity>> =
        dao.getAll().map { list -> list.map(codec::toDomain) }

    override suspend fun upsert(activity: FieldActivity) =
        dao.insert(codec.toEntity(activity))

    override suspend fun delete(id: String) = dao.delete(id)
}
