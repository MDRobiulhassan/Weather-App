package com.example.weatherapp
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray

suspend fun fetchHourlyWeatherForDate(city: String, date: String, tempUnit: String): List<HourlyWeather> {
    val client = OkHttpClient()
    val apiKey = "3b355c17a0634f28950165400250603" // Replace with your API key
    val url = "http://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=8&aqi=no&alerts=yes"
    val request = Request.Builder().url(url).build()

    return try {
        withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to fetch weather: ${response.code}")
                }

                val responseBodyString = response.body?.string()
                    ?: throw IOException("Empty response body")

                val json = JSONObject(responseBodyString)

                // Extract 'forecast' data for specific date
                val forecast = json.optJSONObject("forecast")
                val forecastDay = forecast?.optJSONArray("forecastday")
                val selectedDay = findForecastForDate(forecastDay, date)

                val hourlyArray = selectedDay?.optJSONArray("hour")

                val hourlyWeatherList = mutableListOf<HourlyWeather>()

                // Iterate over each hour and extract the relevant data
                for (i in 0 until (hourlyArray?.length() ?: 0)) {
                    val hour = hourlyArray?.optJSONObject(i)
                    val time = hour?.optString("time", "Unknown")
                    var tempC = hour?.optDouble("temp_c", Double.NaN) ?: Double.NaN
                    val tempF = (tempC * 9/5) + 32 // Convert to Fahrenheit

                    // Convert temperature to the selected unit
                    val temp = if (tempUnit == "Fahrenheit") tempF else tempC

                    // Extract weather condition
                    val condition = hour?.optJSONObject("condition")?.optString("text", "Unknown") ?: "Unknown"
                    val icon = hour?.optJSONObject("condition")?.optString("icon", "")

                    hourlyWeatherList.add(HourlyWeather(time ?: "Unknown", temp.toString(), condition, icon ?: ""))
                }

                // Return the list of hourly weather data
                hourlyWeatherList
            }
        }
    } catch (e: IOException) {
        Log.e("Weather", "Error fetching hourly weather for date", e)
        emptyList() // Return empty list in case of an error
    }
}

// Helper function to find forecast data for a specific date
private fun findForecastForDate(forecastDays: JSONArray?, date: String): JSONObject? {
    for (i in 0 until (forecastDays?.length() ?: 0)) {
        val forecastDay = forecastDays?.optJSONObject(i)
        val forecastDate = forecastDay?.optString("date", "")

        if (forecastDate == date) {
            return forecastDay
        }
    }
    return null // Return null if no forecast found for the given date
}
