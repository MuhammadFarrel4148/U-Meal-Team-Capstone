package com.example.umeal.data.retrofit

import com.example.umeal.data.response.ArticleResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticleApiService {
    @GET("news")
    suspend fun getNews(
        @Query("apikey") apikey: String,
        @Query("q") q: String = "makanan sehat nutrisi",
        @Query("country") country: String = "id"
    ): ArticleResponse
}