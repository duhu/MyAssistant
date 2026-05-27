package com.myassistant.app.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.myassistant.app.data.remote.DeepSeekApiService
import com.myassistant.app.data.remote.MiniMaxApiService
import com.myassistant.app.data.util.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(secureStorage: SecureStorage): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val apiKey = secureStorage.getApiKey()
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("minimax")
    fun provideMiniMaxRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(MiniMaxApiService.BASE_URL_CN)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideMiniMaxApiService(@Named("minimax") retrofit: Retrofit): MiniMaxApiService =
        retrofit.create(MiniMaxApiService::class.java)

    @Provides
    @Singleton
    @Named("deepseek")
    fun provideDeepSeekRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(DeepSeekApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideDeepSeekApiService(@Named("deepseek") retrofit: Retrofit): DeepSeekApiService =
        retrofit.create(DeepSeekApiService::class.java)
}
