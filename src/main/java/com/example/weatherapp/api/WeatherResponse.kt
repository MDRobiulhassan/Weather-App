package com.example.weatherapp.api
data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

data class Location(
    val name: String,
    val country: String
)

data class Current(
    val temp_c: Float,
    val condition: Condition,
    val feelslike_c: Float,
    val humidity: Int,
    val wind_kph: Float
)

data class Condition(
    val text: String,
    val icon: String
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day,
    val astro: Astro,
    val hour: List<Hour>
)

data class Day(
    val maxtemp_c: Float,
    val mintemp_c: Float,
    val condition: Condition
)

data class Astro(
    val sunrise: String,
    val sunset: String
)

data class Hour(
    val time: String,
    val temp_c: Float,
    val condition: Condition
)
