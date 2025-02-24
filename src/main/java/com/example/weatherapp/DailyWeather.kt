package com.example.weatherapp

data class DailyWeather(
    val date: String,
    val temperature: Double, // You can use a different type if necessary
    val condition: String,
    val icon: String
)

