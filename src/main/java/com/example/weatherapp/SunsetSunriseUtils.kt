package com.example.weatherapp

import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import android.util.Log

// Data class to hold the sunrise and sunset times
data class SunsetSunriseData(
    val sunrise: String,
    val sunset: String
)

// Function to make the network request to fetch data (renamed to avoid conflict)
suspend fun sunsetSunriseApiRequest(url: String): JSONObject? {
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
        Log.e("APIRequestError", "Error fetching data: ${e.localizedMessage}")
        null // Return null if there was an error
    }
}

// Function to fetch sunrise and sunset times from the API response
fun parseSunsetSunrise(response: JSONObject): SunsetSunriseData? {
    return try {
        val astro = response.getJSONObject("forecast")
            .getJSONArray("forecastday")
            .getJSONObject(0)
            .getJSONObject("astro")

        val sunrise = astro.getString("sunrise")
        val sunset = astro.getString("sunset")

        SunsetSunriseData(sunrise, sunset)
    } catch (e: Exception) {
        // Catching any exceptions during JSON parsing
        Log.e("ParseError", "Error parsing response: ${e.localizedMessage}")
        null
    }
}

// Function to fetch sunset and sunrise times for a city
suspend fun fetchSunsetSunrise(city: String): SunsetSunriseData? {
    val apiKey = "06c121bc7bec4a3ca01141936250502" // Replace with your actual API key
    val apiUrl = "https://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=8&aqi=no&alerts=yes" // Changed to HTTPS

    return try {
        val response = sunsetSunriseApiRequest(apiUrl)  // Call the renamed function
        response?.let {
            // Parse sunrise and sunset data from the response
            parseSunsetSunrise(it)
        }
    } catch (e: Exception) {
        // Handle error (e.g., log the error, show a message)
        Log.e("FetchError", "Error fetching sunset/sunrise data: ${e.localizedMessage}")
        null
    }
}
