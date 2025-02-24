package com.example.weatherapp

data class HourlyWeather(
    val time: String,
    val temperature: String,
    val condition: String,
    val icon: String // You can adjust this to suit your icon handling (e.g., use icon URL or resource ID)
)
