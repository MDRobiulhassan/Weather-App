package com.example.weatherapp

data class WeatherApiResponse(
    val daily: List<DailyForecast>
)

data class DailyForecast(
    val date: String, // Date in Unix timestamp or string format
    val temp: Temperature,
    val weather: List<WeatherCondition>
)

data class Temperature(
    val day: Double // Day temperature in Celsius
)

data class WeatherCondition(
    val main: String // Condition like "Clear", "Rain", etc.
)
