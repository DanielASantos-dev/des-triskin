package br.com.triskin.di

import br.com.triskin.data.repository.FieldActivityRepositoryImpl
import br.com.triskin.data.repository.TaskRepositoryImpl
import br.com.triskin.data.repository.WeatherRepositoryImpl
import br.com.triskin.domain.repository.FieldActivityRepository
import br.com.triskin.domain.repository.TaskRepository
import br.com.triskin.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindFieldActivityRepository(impl: FieldActivityRepositoryImpl): FieldActivityRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository
}
