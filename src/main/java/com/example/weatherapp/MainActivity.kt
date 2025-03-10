package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // Initialize Firebase

        auth = FirebaseAuth.getInstance()

        // Request notification permission (for Android 13 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // Check if the user is logged in
        val user = auth.currentUser
        val startDestination = if (user != null) "home" else "login"

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = startDestination) {
                composable("login") { LoginPage(navController) }
                composable("signup") { SignupPage(navController) }
                composable("home") { HomeScreen(navController) }
                composable("location") { LocationPage(navController) }
                composable("setting") { SettingPage(navController) }
                composable("savedcities") { SavedCitiesPage(navController) }
                composable("SavedCityScreen/{city}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    SavedCityScreen(navController, city)
                }

                composable("daily_details/{city}/{date}") { backStackEntry ->
                    val city = backStackEntry.arguments?.getString("city") ?: ""
                    val date = backStackEntry.arguments?.getString("date") ?: ""
                    DailyDetails(navController,city, date)
                }
            }
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule notifications
                scheduleNotifications(this)
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
