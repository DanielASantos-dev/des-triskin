package br.com.triskin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.triskin.data.local.entity.FieldActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldActivityDao {
    @Query("SELECT * FROM field_activities ORDER BY startedAt DESC")
    fun getAll(): Flow<List<FieldActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: FieldActivityEntity)

    @Query("DELETE FROM field_activities WHERE id = :id")
    suspend fun delete(id: String)
}
