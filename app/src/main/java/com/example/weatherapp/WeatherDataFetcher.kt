package com.example.weatherapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

suspend fun fetchHourlyForecast(city: String, tempUnit: String): Pair<HourlyWeather?, List<String>> {
    val client = OkHttpClient()
    val apiKey = "06c121bc7bec4a3ca01141936250502" // Replace with your API key
    val url = "http://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=1&aqi=no&alerts=yes"

    return try {
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Failed to fetch weather: ${response.code}")

                val responseBodyString = response.body?.string() ?: throw IOException("Empty response body")
                val json = JSONObject(responseBodyString)

                // Extract current weather from the "current" object
                val currentWeather = json.optJSONObject("current")?.let { current ->
                    val temperature = if (tempUnit == "Fahrenheit") {
                        "${current.optDouble("temp_f", Double.NaN).toInt()}°F"
                    } else {
                        "${current.optDouble("temp_c", Double.NaN).toInt()}°C"
                    }
                    val condition = current.optJSONObject("condition")?.optString("text", "Unknown") ?: "Unknown"
                    HourlyWeather("Now", temperature, condition, "")
                }

                // Extract alerts from the hourly forecast
                val alerts = mutableListOf<String>()
                val forecast = json.optJSONObject("forecast")?.optJSONArray("forecastday")
                forecast?.optJSONObject(0)?.optJSONArray("hour")?.let { hours ->
                    for (i in 0 until hours.length()) {
                        val hour = hours.optJSONObject(i)
                        val time = hour?.optString("time", "Unknown")?.let { fullTime ->
                            if (fullTime.contains(" ")) {
                                fullTime.split(" ")[1] // Extract time part
                            } else {
                                "Unknown" // Fallback if the time format is unexpected
                            }
                        } ?: "Unknown" // Fallback if the time is null

                        val condition = hour?.optJSONObject("condition")?.optString("text", "Unknown") ?: "Unknown"
                        val tempC = hour?.optDouble("temp_c", Double.NaN) ?: Double.NaN

                        // Check for rain, snow, or extreme heat
                        if (condition.contains("rain", ignoreCase = true)) {
                            alerts.add("Rain expected at $time")
                        }
                        if (condition.contains("snow", ignoreCase = true)) {
                            alerts.add("Snow expected at $time")
                        }
                        if (tempC > 35) { // Extreme heat threshold
                            alerts.add("Extreme heat expected at $time")
                        }
                    }
                }

                Pair(currentWeather, alerts)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Pair(null, emptyList())
    }
}