package com.example.umeal.data.retrofit

import com.example.umeal.data.response.ResponseLogin
import com.example.umeal.data.response.ResponseRegister
import com.example.umeal.home.ui.scan.ResponseScanImage
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") auth: String,
    )

    @Multipart
    @POST("scanimage")
    suspend fun scanImage(
        @Header("Authorization") auth: String,
        @Part file: MultipartBody.Part,
    ): Response<ResponseScanImage>
}