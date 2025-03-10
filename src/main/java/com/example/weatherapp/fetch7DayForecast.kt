package com.example.weatherapp
import android.app.VoiceInteractor
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

suspend fun fetch7DayForecast(city: String, tempUnit: String): List<DailyWeather> {
    val client = OkHttpClient()
    val apiKey = "3b355c17a0634f28950165400250603" // Replace with your API key
    val url = "http://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=8&aqi=no&alerts=yes" // `days=7` for 7-day forecast
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

                // Extract 'forecast' data for daily forecast
                val forecast = json.optJSONObject("forecast")
                val forecastDays = forecast?.optJSONArray("forecastday")

                val dailyWeatherList = mutableListOf<DailyWeather>()

                // Iterate over each day and extract the relevant data
                for (i in 1 until (forecastDays?.length() ?: 0)) {
                    val day = forecastDays?.optJSONObject(i)
                    val date = day?.optString("date", "Unknown")
                    var tempCMin = day?.optJSONObject("day")?.optDouble("mintemp_c", Double.NaN) ?: Double.NaN
                    var tempCMax = day?.optJSONObject("day")?.optDouble("maxtemp_c", Double.NaN) ?: Double.NaN

                    val tempFMin = (tempCMin * 9/5) + 32 // Convert to Fahrenheit
                    val tempFMax = (tempCMax * 9/5) + 32 // Convert to Fahrenheit

                    // Convert temperature to the selected unit
                    val tempMin = if (tempUnit == "Fahrenheit") tempFMin else tempCMin
                    val tempMax = if (tempUnit == "Fahrenheit") tempFMax else tempCMax

                    // Extract weather condition for the day
                    val condition = day?.optJSONObject("day")?.optJSONObject("condition")?.optString("text", "Unknown") ?: "Unknown"
                    val icon = day?.optJSONObject("day")?.optJSONObject("condition")?.optString("icon", "")

                    dailyWeatherList.add(DailyWeather(date ?: "Unknown", tempMax, condition, icon ?: ""))
                }

                // Return the list of daily weather data
                dailyWeatherList
            }
        }
    } catch (e: IOException) {
        Log.e("Weather", "Error fetching daily weather", e)
        emptyList() // Return empty list in case of an error
    }
}

