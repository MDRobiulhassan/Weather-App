package com.example.weatherapp
import android.util.Log
import com.example.weatherapp.Weather
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun fetchWeather(city: String, tempUnit: String): Weather {
    val client = OkHttpClient()
    val apiKey = "06c121bc7bec4a3ca01141936250502" // Replace with your API key
    val url = "http://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$city&days=1&aqi=no&alerts=yes"
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

                // Extract 'current' weather data
                val current = json.optJSONObject("current")
                val condition = current?.optJSONObject("condition")?.optString("text", "Unknown") ?: "Unknown"
                var tempC = current?.optDouble("temp_c", Double.NaN) ?: Double.NaN
                val tempF = (tempC * 9/5) + 32 // Convert to Fahrenheit

                // Convert temperature to the selected unit
                val temp = if (tempUnit == "Fahrenheit") tempF else tempC

                // Extract 'forecast' data for min and max temperatures
                val forecast = json.optJSONObject("forecast")
                val forecastDay = forecast?.optJSONArray("forecastday")?.optJSONObject(0)
                val day = forecastDay?.optJSONObject("day")

                val minTempC = day?.optDouble("mintemp_c", Double.NaN) ?: Double.NaN
                val maxTempC = day?.optDouble("maxtemp_c", Double.NaN) ?: Double.NaN
                val minTemp = if (tempUnit == "Fahrenheit") (minTempC * 9/5) + 32 else minTempC
                val maxTemp = if (tempUnit == "Fahrenheit") (maxTempC * 9/5) + 32 else maxTempC

                // Return the Weather object with all parsed values
                Weather(condition, temp.toString(), minTemp.toString(), maxTemp.toString())
            }
        }
    } catch (e: IOException) {
        Log.e("Weather", "Error fetching weather", e)
        Weather("Unknown", "Unknown", "Unknown", "Unknown")
    }
}




