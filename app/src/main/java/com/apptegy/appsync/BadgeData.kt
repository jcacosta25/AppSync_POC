package com.apptegy.appsync


import com.google.gson.annotations.SerializedName

data class BadgeData(
    @SerializedName("classwork")
    val classwork: Int?,
    @SerializedName("messages")
    val messages: Int?,
    @SerializedName("stream")
    val stream: Int?
)