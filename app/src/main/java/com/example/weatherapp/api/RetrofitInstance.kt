package com.example.weatherapp.api

import com.example.weatherapp.api.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("forecast.json")
    suspend fun getWeatherData(
        @Query("key") apiKey: String,
        @Query("q") cityName: String,
        @Query("days") days: Int = 7,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "yes"
    ): WeatherResponse
}

