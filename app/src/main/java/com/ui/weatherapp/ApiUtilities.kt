package com.ui.weatherapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    private var retrofit:Retrofit?=null
    var BASEURL ="https://api.openweathermap.org/data/2.5/"
    fun getAPiInterface():ApiInterface?{
        if(retrofit==null){
            retrofit=Retrofit.Builder().baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
        return retrofit!!.create(ApiInterface::class.java)
    }
}