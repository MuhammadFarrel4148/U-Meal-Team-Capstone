package com.example.umeal.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.umeal.BuildConfig
import com.example.umeal.data.ResultState
import com.example.umeal.data.response.ArticleResponse
import com.example.umeal.data.response.ResultsItem
import com.example.umeal.data.retrofit.ArticleApiService
import com.google.gson.Gson
import retrofit2.HttpException

class ArticleRepository(
    private val apiService: ArticleApiService,
) {
    fun getArticles(): LiveData<ResultState<List<ResultsItem>>> = liveData {
        emit(ResultState.Loading())
        try {
            val successResponse = apiService.getNews(apikey = BuildConfig.ARTICLE_API_KEY)
            emit(ResultState.Success(successResponse.results))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.toString()
            val errorResponse = Gson().fromJson(errorBody, ArticleResponse::class.java)
            emit(ResultState.Error(errorResponse.status ?: "Unknown Error"))
        }
    }

    companion object {
        @Volatile
        private var instance: ArticleRepository? = null
        fun getInstance(
            articleApiService: ArticleApiService
        ): ArticleRepository = instance ?: synchronized(this) {
            instance ?: ArticleRepository(articleApiService)
        }.also { instance = it }
    }
}