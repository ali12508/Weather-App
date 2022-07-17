package com.ui.weatherapp

import com.google.gson.annotations.SerializedName

data class ModalClass(
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("main") val main: Main,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("name") val name : String
    )
