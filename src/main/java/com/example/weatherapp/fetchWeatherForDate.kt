package com.example.weatherapp
import android.util.Log
import com.example.weatherapp.Weather
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchWeatherForDate(city: String, date: String, tempUnit: String): Weather {
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

                // Extract 'forecast' data for the specific date
                val forecast = json.optJSONObject("forecast")
                val forecastDays = forecast?.optJSONArray("forecastday")
                var weatherData: Weather = Weather("Unknown", "Unknown", "Unknown", "Unknown")

                // Iterate through forecastDays to find the data for the specific date
                for (i in 0 until (forecastDays?.length() ?: 0)) {
                    val forecastDay = forecastDays?.optJSONObject(i)
                    val forecastDate = forecastDay?.optString("date", "")

                    if (forecastDate == date) { // Check if the forecast date matches the provided date
                        val day = forecastDay?.optJSONObject("day")
                        val condition = day?.optJSONObject("condition")?.optString("text", "Unknown") ?: "Unknown"
                        val avgTempC = day?.optDouble("avgtemp_c", Double.NaN) ?: Double.NaN
                        val minTempC = day?.optDouble("mintemp_c", Double.NaN) ?: Double.NaN
                        val maxTempC = day?.optDouble("maxtemp_c", Double.NaN) ?: Double.NaN

                        // Convert temperature to the selected unit
                        val avgTemp = if (tempUnit == "Fahrenheit") (avgTempC * 9/5) + 32 else avgTempC
                        val minTemp = if (tempUnit == "Fahrenheit") (minTempC * 9/5) + 32 else minTempC
                        val maxTemp = if (tempUnit == "Fahrenheit") (maxTempC * 9/5) + 32 else maxTempC

                        // Update weatherData
                        weatherData = Weather(condition, avgTemp.toString(), minTemp.toString(), maxTemp.toString())
                        break
                    }
                }


                weatherData
            }
        }
    } catch (e: IOException) {
        Log.e("Weather", "Error fetching weather for date", e)
        Weather("Unknown", "Unknown", "Unknown", "Unknown")
    }
}

