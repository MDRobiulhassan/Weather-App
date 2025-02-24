package com.example.weatherapp

import org.json.JSONObject
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Data class to hold weather statistics
data class WeatherStatsData(
    val feelsLike: String,  // Change to String to include unit (e.g., "22.5°C")
    val humidity: Int,
    val windSpeed: String,  // Change to String to include unit (e.g., "15 km/h")
    val tempUnit: String,
    val windUnit: String
)


// Function to fetch weather data based on city and user preferences
suspend fun fetchWeatherStats(city: String, preferences: UserPreferences): WeatherStatsData? {
    val apiKey = "06c121bc7bec4a3ca01141936250502" // Replace with your actual API key
    val apiUrl = "http://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=8&aqi=no&alerts=yes"

    return try {
        // Make the network request to fetch weather data
        val response = apiRequest(apiUrl)

        // Parse the weather data from the response
        val temp = parseTemperature(response, preferences.tempUnit)
        val windSpeed = parseWindSpeed(response, preferences.windSpeedUnit)
        val feelsLike = parseFeelsLike(response, preferences.tempUnit)
        val humidity = parseHumidity(response)

        // Return the WeatherStatsData object with parsed data
        WeatherStatsData(feelsLike, humidity, windSpeed, preferences.tempUnit, preferences.windSpeedUnit)
    } catch (e: Exception) {
        // Handle the error (e.g., log the error, show a message)
        null
    }
}

// Function to make the network request (this can be done using Retrofit, Ktor, etc.)
suspend fun apiRequest(url: String): JSONObject {
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
