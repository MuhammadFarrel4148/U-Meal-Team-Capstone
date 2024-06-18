package com.example.umeal.data.retrofit

import com.example.umeal.data.response.ForgotPasswordResponse
import com.example.umeal.data.response.ResponseHistory
import com.example.umeal.data.response.ResponseLogin
import com.example.umeal.data.response.ResponseRegister
import com.example.umeal.data.response.ScanFoodResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("forgotpassword")
    @FormUrlEncoded
    suspend fun forgotPassword(
        @Field("email") email: String
    ): Response<ForgotPasswordResponse>

    @POST("changepassword")
    @FormUrlEncoded
    suspend fun changePassword(
        @Field("codeotp") codeotp: String,
        @Field("newPassword") newPassword: String
    ): Response<ForgotPasswordResponse>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") auth: String,
    )

    @Multipart
    @POST("scanimage")
    suspend fun scanImage(
        @Header("Authorization") auth: String,
        @Part file: MultipartBody.Part,
    ): Response<ScanFoodResponse>

    @GET("history/{id}")
    suspend fun getHistory(
        @Header("Authorization") auth: String,
        @Path("id") id: String,
        @Query("taggal") date: String,
        @Query("bulan") month: String,
        @Query("tahun") year: String,
    ): Response<ResponseHistory>
}