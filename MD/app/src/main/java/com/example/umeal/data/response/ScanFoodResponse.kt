package com.example.umeal.data.response

import com.google.gson.annotations.SerializedName

data class ScanFoodResponse(

	@field:SerializedName("data")
	val data: Data,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("status")
	val status: String
)

data class DetectedFoodsItem(

	@field:SerializedName("kalori")
	val kalori: Int,

	@field:SerializedName("jenis")
	val jenis: String
)

data class Data(

	@field:SerializedName("detectedFoods")
	val detectedFoods: List<DetectedFoodsItem?>,

	@field:SerializedName("scanId")
	val scanId: Int,

	@field:SerializedName("imageUrl")
	val imageUrl: String,

	@field:SerializedName("scanTimestamp")
	val scanTimestamp: String,

	@field:SerializedName("totalCalories")
	val totalCalories: Int
)
