package com.example.weatherapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherNotificationWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val city = getCityFromFirebase() // Fetch city dynamically
                    val tempUnit = "Celsius" // Replace with the actual unit or get it from user preferences
                    val (currentWeather, alerts) = fetchHourlyForecast(city, tempUnit)

                    // Send current weather notification
                    currentWeather?.let {
                        val title = "Weather Update"
                        val content = "Temperature: ${it.temperature}, Condition: ${it.condition}"
                        WeatherNotificationScheduler.sendCurrentWeatherNotification(applicationContext, title, content)
                    }

                    // Send alerts if any
                    if (alerts.isNotEmpty()) {
                        val alertTitle = "Weather Alerts"
                        val alertContent = alerts.joinToString("\n")
                        WeatherNotificationScheduler.sendAlertNotification(applicationContext, alertTitle, alertContent)
                    }

                    Result.success()
                } else {
                    // User is not logged in, do not send notifications
                    Result.failure()
                }
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}