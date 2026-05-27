package com.myassistant.app.di

import com.myassistant.app.data.repository.AIAnalysisRepositoryImpl
import com.myassistant.app.data.repository.CardRepositoryImpl
import com.myassistant.app.domain.repository.AIAnalysisRepository
import com.myassistant.app.domain.repository.CardRepository
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
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds
    @Singleton
    abstract fun bindAIAnalysisRepository(impl: AIAnalysisRepositoryImpl): AIAnalysisRepository
}
