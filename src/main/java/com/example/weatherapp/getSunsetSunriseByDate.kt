package com.example.weatherapp

import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import android.util.Log

// Function to extract sunrise and sunset for a specific date
fun extractSunsetSunrise(response: JSONObject, date: String): SunsetSunriseData? {
    return try {
        val forecastArray = response.getJSONObject("forecast").getJSONArray("forecastday")

        for (i in 0 until forecastArray.length()) {
            val dayData = forecastArray.getJSONObject(i)

            // Check if the date matches the requested date
            if (dayData.getString("date") == date) {
                val astro = dayData.getJSONObject("astro")

                val sunrise = astro.getString("sunrise")
                val sunset = astro.getString("sunset")

                return SunsetSunriseData(sunrise, sunset)
            }
        }

        // If date not found, log a warning
        Log.w("DataWarning", "Date $date not found in API response")
        null
    } catch (e: Exception) {
        Log.e("ParseError", "Error parsing response: ${e.localizedMessage}")
        null
    }
}

// Function to fetch sunrise and sunset for a city on a specific date
suspend fun getSunsetSunriseByDate(city: String, date: String): SunsetSunriseData? {
    val apiKey = "3b355c17a0634f28950165400250603" // Replace with your actual API key
    val apiUrl = "http://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=8&aqi=no&alerts=no"

    return try {
        val response = makeApiRequest(apiUrl)
        response?.let {
            extractSunsetSunrise(it, date) // Now correctly searches for the date in the response
        }
    } catch (e: Exception) {
        Log.e("FetchError", "Error fetching sunset/sunrise data: ${e.localizedMessage}")
        null
    }
}
