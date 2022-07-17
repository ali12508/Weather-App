package com.ui.weatherapp


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getCurrentWeatherdata(
        @Query("lat") latitude:String,
        @Query("lon") longitude:String,
        @Query("APPID") apiKey:String)
            : Call<ModalClass>

    @GET("weather")
    fun getCityWeatherdata(
        @Query("q") Cityname:String,
        @Query("APPID") apiKey:String):Call<ModalClass>

}