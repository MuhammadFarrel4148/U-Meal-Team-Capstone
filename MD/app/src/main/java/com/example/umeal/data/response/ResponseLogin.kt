package com.example.umeal.data.response

import com.google.gson.annotations.SerializedName

data class ResponseLogin(

	@field:SerializedName("data")
	val data: UserData,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String,

	@field:SerializedName("token")
	val token: String
)

data class UserData(

	@field:SerializedName("user")
	val user: User
)

data class User(

	@field:SerializedName("phonenumber")
	val phonenumber: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("email")
	val email: String,

	@field:SerializedName("username")
	val username: String
)
