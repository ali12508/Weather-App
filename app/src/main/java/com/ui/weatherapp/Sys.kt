package com.ui.weatherapp

import com.google.gson.annotations.SerializedName

data class Sys(
    @SerializedName( "type") val type:Double,
    @SerializedName( "id") val id:Int,
    @SerializedName( "country") val country:String,
    @SerializedName( "sunrise") val sunrise:Int,
    @SerializedName( "sunset") val sunset:Int


    )
