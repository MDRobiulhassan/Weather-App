package com.example.weatherapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WeatherNotificationScheduler {
    private const val CHANNEL_ID = "weather_notifications_channel"
    private const val CURRENT_WEATHER_NOTIFICATION_ID = 1
    private const val ALERT_NOTIFICATION_ID = 2

    // Initialize Notification Channel (Required for Android Oreo and above)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts"
            val descriptionText = "Get weather alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Check if the permission is granted
    private fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // For older versions, permissions are handled differently
        }
    }

    // Request notification permission (Android 13+)
    private fun requestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                context as ComponentActivity,  // Replace with your Activity if needed
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }

    // Send current weather notification
    fun sendCurrentWeatherNotification(context: Context, title: String, content: String) {
        sendNotification(context, title, content, CURRENT_WEATHER_NOTIFICATION_ID)
    }

    // Send alert notification
    fun sendAlertNotification(context: Context, title: String, content: String) {
        sendNotification(context, title, content, ALERT_NOTIFICATION_ID)
    }

    // Private method to send notifications
    fun sendNotification(context: Context, title: String, content: String, notificationId: Int) {
        try {
            if (isNotificationPermissionGranted(context)) {
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.appicon)  // Replace with your own icon
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)

                // Create an explicit intent for notification tap action
                val intent = Intent(context, MainActivity::class.java)

                // Handle PendingIntent based on API version
                val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                } else {
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                builder.setContentIntent(pendingIntent)

                // Send notification
                val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
                notificationManager.notify(notificationId, builder.build())
            } else {
                // Notify the user that they need to grant permission
                Toast.makeText(context, "Please enable notifications in settings", Toast.LENGTH_SHORT).show()
                requestNotificationPermission(context)
            }
        } catch (e: SecurityException) {
            // Handle the case where permissions are not granted or a security exception occurs
            Toast.makeText(context, "Notification permission required", Toast.LENGTH_SHORT).show()
        }
    }
}