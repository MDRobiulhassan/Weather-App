package com.example.weatherapp

import android.content.Context
import android.widget.Toast
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.weatherapp.WeatherNotificationScheduler.createNotificationChannel
import com.example.weatherapp.WeatherNotificationScheduler.sendNotification
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun scheduleNotifications(context: Context) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        // User is logged in, schedule weather notifications
        WeatherNotificationScheduler.createNotificationChannel(context)

        // Fetch and send the current weather and alerts immediately
        CoroutineScope(Dispatchers.IO).launch {
            val city = getCityFromFirebase() // Fetch city dynamically
            val tempUnit = "Celsius" // Replace with the actual unit or get it from user preferences
            val (currentWeather, alerts) = fetchHourlyForecast(city, tempUnit)

            // Send current weather notification
            currentWeather?.let {
                val title = "Current Weather"
                val content = "Temperature: ${it.temperature}, Condition: ${it.condition}"
                WeatherNotificationScheduler.sendCurrentWeatherNotification(context, title, content)
            }

            // Send alerts if any
            if (alerts.isNotEmpty()) {
                val alertTitle = "Weather Alerts"
                val alertContent = alerts.joinToString("\n")
                WeatherNotificationScheduler.sendAlertNotification(context, alertTitle, alertContent)
            }
        }

        // Schedule periodic notifications every hour
        val weatherWorkRequest = PeriodicWorkRequest.Builder(
            WeatherNotificationWorker::class.java,
            1, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueue(weatherWorkRequest)
    } else {
        // If the user is not logged in, we don't schedule notifications
        Toast.makeText(context, "Please login to receive notifications", Toast.LENGTH_SHORT).show()
    }
}