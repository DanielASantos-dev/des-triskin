package br.com.triskin.domain.repository

import br.com.triskin.domain.model.FieldActivity
import kotlinx.coroutines.flow.Flow

interface FieldActivityRepository {
    fun getAll(): Flow<List<FieldActivity>>
    suspend fun upsert(activity: FieldActivity)
    suspend fun delete(id: String)
}
