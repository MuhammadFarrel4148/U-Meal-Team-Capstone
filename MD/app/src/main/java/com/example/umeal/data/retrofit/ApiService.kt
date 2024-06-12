package com.example.umeal.data.retrofit

import com.example.umeal.data.response.ResponseLogin
import com.example.umeal.data.response.ResponseRegister
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @POST("signin")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<ResponseLogin>

    @POST("signup")
    @FormUrlEncoded
    suspend fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("phonenumber") phoneNumber: String,
        @Field("password") password: String
    ): Response<ResponseRegister>
}