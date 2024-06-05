package com.example.umeal.di

import android.content.Context
import com.example.umeal.data.repository.ArticleRepository
import com.example.umeal.data.retrofit.ArticleApiConfig

object Injection {
    fun provideArticleRepository(context: Context): ArticleRepository {
        val apiService = ArticleApiConfig.getApiService()
        return ArticleRepository.getInstance(apiService)
    }
}