package com.example.weatherapp

import org.json.JSONObject
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Function to fetch weather data based on city, date, and user preferences
suspend fun fetchWeatherStatsForDate(city: String, date: String, preferences: UserPreferences): WeatherStatsData? {
    val apiKey = "3b355c17a0634f28950165400250603" // Replace with your actual API key
    val apiUrl = "http://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=8&aqi=no&alerts=yes"

    return try {
        // Make the network request to fetch weather data
        val weatherResponse = makeApiRequest(apiUrl)

        // Find the forecast data for the given date
        val forecastDataForDate = extractForecastForDate(weatherResponse, date)

        if (forecastDataForDate == null) {
            return null // No forecast data found for the given date
        }

        // Parse the weather data for that specific date
        val temp = parseTemperatureForDate(forecastDataForDate, preferences.tempUnit)
        val windSpeed = parseWindSpeedForDate(forecastDataForDate, preferences.windSpeedUnit)
        val feelsLike = parseFeelsLikeForDate(forecastDataForDate, preferences.tempUnit)
        val humidity = parseHumidityForDate(forecastDataForDate)

        // Return the WeatherStatsData object with parsed data
        WeatherStatsData(feelsLike, humidity, windSpeed, preferences.tempUnit, preferences.windSpeedUnit)
    } catch (e: Exception) {
        // Handle the error (e.g., log the error, show a message)
        null
    }
}

// Function to make the network request (this can be done using Retrofit, Ktor, etc.)
suspend fun makeApiRequest(url: String): JSONObject {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return try {
        withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("Failed to fetch weather: ${response.code}")
            }
            val responseBodyString = response.body?.string()
                ?: throw IOException("Empty response body")

            // Parse the response body string into a JSONObject
            JSONObject(responseBodyString)
        }
    } catch (e: IOException) {
        // Handle network errors (e.g., log the error, show a message)
        throw e // Re-throw the exception to propagate it up
    }
}

// Helper function to find the forecast data for the specified date
private fun extractForecastForDate(response: JSONObject, date: String): JSONObject? {
    val forecast = response.optJSONObject("forecast")
    val forecastDays = forecast?.optJSONArray("forecastday")

    // Loop through the forecast days and match the date
    for (i in 0 until (forecastDays?.length() ?: 0)) {
        val forecastDay = forecastDays?.optJSONObject(i)
        val forecastDate = forecastDay?.optString("date", "")

        // Match the forecast date to the requested date
        if (forecastDate == date) {
            return forecastDay
        }
    }
    return null // If no matching date found, return null
}

// Helper function to parse temperature based on user preferences (Integer now)
private fun parseTemperatureForDate(forecastDay: JSONObject, tempUnit: String): String {
    val day = forecastDay.optJSONObject("day")
    val tempC = day?.optDouble("avgtemp_c", Double.NaN) ?: Double.NaN
    val tempF = ((tempC * 9/5) + 32).toInt() // Convert to Fahrenheit and round to integer

    return if (tempUnit == "Fahrenheit") {
        "$tempF°F"
    } else {
        tempC.toInt().toString() + "°C" // Convert to integer and return in Celsius
    }
}

// Helper function to parse wind speed based on user preferences (now in km/h or mph)
private fun parseWindSpeedForDate(forecastDay: JSONObject, windSpeedUnit: String): String {
    val day = forecastDay.optJSONObject("day")

    if (day == null) {
        println("Warning: Day data is not available.")
        return "N/A"
    }

    // Get the wind speed in km/h from the forecast day data
    val windSpeedKph = day.optDouble("maxwind_kph", Double.NaN)

    if (windSpeedKph.isNaN()) {
        println("Warning: maxwind_kph not found in the response.")
        return "N/A"
    }

    // Log the raw wind speed value in km/h
    println("Raw windSpeedKph: $windSpeedKph")

    // Convert wind speed based on the unit selected
    val windSpeed = if (windSpeedUnit == "km/h") {
        windSpeedKph.toInt()
    } else {
        // Convert from km/h to mph (1 km/h = 0.621371 mph)
        (windSpeedKph * 0.621371).toInt()
    }

    // Log the converted wind speed value
    println("Converted Wind Speed: $windSpeed")

    // Return the formatted wind speed based on the selected unit
    return if (windSpeedUnit == "km/h") {
        "$windSpeed kmh"
    } else {
        "$windSpeed mph"
    }
}


// Helper function to parse feels like temperature (Integer now)
private fun parseFeelsLikeForDate(forecastDay: JSONObject, tempUnit: String): String {
    val day = forecastDay.optJSONObject("day")
    val feelsLikeC = day?.optDouble("avgtemp_c", Double.NaN) ?: Double.NaN
    val feelsLikeF = ((feelsLikeC * 9/5) + 32).toInt() // Convert to Fahrenheit and round to integer

    return if (tempUnit == "Fahrenheit") {
        "$feelsLikeF°F"
    } else {
        feelsLikeC.toInt().toString() + "°C" // Convert to integer and return in Celsius
    }
}

// Helper function to parse humidity
private fun parseHumidityForDate(forecastDay: JSONObject): Int {
    val day = forecastDay.optJSONObject("day")
    return day?.optInt("avghumidity", -1) ?: -1
}
