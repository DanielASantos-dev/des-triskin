package br.com.triskin.di

import br.com.triskin.BuildConfig
import br.com.triskin.data.remote.api.GeocodingApiService
import br.com.triskin.data.remote.api.WeatherApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.WEATHER_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    @Named("geocoding")
    fun provideGeocodingRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.GEOCODING_API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideWeatherApiService(@Named("weather") retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideGeocodingApiService(@Named("geocoding") retrofit: Retrofit): GeocodingApiService =
        retrofit.create(GeocodingApiService::class.java)
}
