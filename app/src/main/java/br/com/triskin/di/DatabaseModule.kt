package br.com.triskin.di

import android.content.Context
import androidx.room.Room
import br.com.triskin.data.local.MIGRATION_1_2
import br.com.triskin.data.local.MIGRATION_2_3
import br.com.triskin.data.local.MIGRATION_3_4
import br.com.triskin.data.local.TriskinDatabase
import br.com.triskin.data.local.dao.CachedWeatherDao
import br.com.triskin.data.local.dao.FieldActivityDao
import br.com.triskin.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TriskinDatabase =
        Room.databaseBuilder(context, TriskinDatabase::class.java, "triskin_db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides
    fun provideTaskDao(database: TriskinDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideFieldActivityDao(database: TriskinDatabase): FieldActivityDao =
        database.fieldActivityDao()

    @Provides
    fun provideCachedWeatherDao(database: TriskinDatabase): CachedWeatherDao =
        database.cachedWeatherDao()
}
