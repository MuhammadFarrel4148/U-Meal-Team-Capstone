package com.example.umeal.di

import android.content.Context
import com.example.umeal.data.repository.ArticleRepository
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.data.retrofit.ArticleApiConfig

object Injection {
    fun provideArticleRepository(context: Context): ArticleRepository {
        val articleApiService = ArticleApiConfig.getApiService()
        return ArticleRepository.getInstance(articleApiService)
    }

    fun provideDataRepository(context: Context): DataRepository {
        val apiService = ApiConfig.getApiService()
        return DataRepository.getInstance(apiService)
    }
}
