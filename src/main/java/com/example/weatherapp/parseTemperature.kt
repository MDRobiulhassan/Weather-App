package com.example.weatherapp

import org.json.JSONObject

fun parseTemperature(response: JSONObject, unit: String): Double {
    val forecast = response.optJSONObject("forecast")?.optJSONArray("forecastday")?.getJSONObject(0)
    if (forecast == null) {
        println("Warning: Forecast data is not available.")
        return Double.NaN
    }

    val tempC = forecast.optJSONObject("day")?.optDouble("avgtemp_c", Double.NaN) ?: Double.NaN
    if (tempC.isNaN()) {
        println("Warning: avgtemp_c not found in the response.")
    }

    return if (unit == "Celsius") {
        tempC
    } else {
        (tempC * 9 / 5) + 32
    }
}

fun parseFeelsLike(response: JSONObject, unit: String): String {
    val current = response.optJSONObject("current")
    if (current == null) {
        println("Warning: Current data is not available.")
        return "N/A"
    }

    val feelsLikeC = current.optDouble("feelslike_c", Double.NaN)
    if (feelsLikeC.isNaN()) {
        println("Warning: feelslike_c not found in the response.")
    }

    // Round the feelsLike to integer for no decimals
    val feelsLike = if (unit == "Celsius") {
        feelsLikeC.toInt()
    } else {
        ((feelsLikeC * 9 / 5) + 32).toInt()
    }

    return if (unit == "Celsius") {
        "$feelsLike°C"
    } else {
        "$feelsLike°F"
    }
}


fun parseWindSpeed(response: JSONObject, unit: String): String {
    // Get the forecast data (assuming only one day's forecast)
    val forecast = response.optJSONObject("current")
    if (forecast == null) {
        println("Warning: Current data is not available.")
        return "N/A"
    }

    // Get the wind speed in km/h from the JSON response
    val windSpeedKph = forecast.optDouble("wind_kph", Double.NaN)
    if (windSpeedKph.isNaN()) {
        println("Warning: wind_kph not found in the response.")
        return "N/A"
    }

    // Log the raw wind speed value in km/h
    println("Raw windSpeedKph: $windSpeedKph")

    // Convert wind speed based on the unit selected
    val windSpeed = if (unit == "km/h") {
        windSpeedKph.toInt()
    } else {
        // Convert from km/h to mph (1 km/h = 0.621371 mph)
        (windSpeedKph * 0.621371).toInt()
    }

    // Log the converted wind speed value
    println("Converted Wind Speed: $windSpeed")

    // Return the formatted wind speed based on the selected unit
    return if (unit == "km/h") {
        "$windSpeed kmh"
    } else {
        "$windSpeed mph"
    }
}





fun parseHumidity(response: JSONObject): Int {
    val forecast = response.optJSONObject("forecast")?.optJSONArray("forecastday")?.getJSONObject(0)
    if (forecast == null) {
        println("Warning: Forecast data is not available.")
        return 0
    }

    val humidity = forecast.optJSONObject("day")?.optInt("avghumidity", 0) ?: 0
    if (humidity == 0) {
        println("Warning: avghumidity not found in the response.")
    }
    return humidity
}


