package com.example.weatherapp

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Data class to hold the user preferences
data class UserPreferences(
    val tempUnit: String,  // "Celsius" or "Fahrenheit"
    val windSpeedUnit: String // "km/h" or "mph"
)

// Function to fetch user preferences (temperature unit and wind speed unit) from Firestore
suspend fun getUserPreferences(userId: String): UserPreferences {
    val db = FirebaseFirestore.getInstance()
    val doc = db.collection("users").document(userId).get().await()
    val tempUnit = doc.getString("tempUnit") ?: "Celsius"
    val windSpeedUnit = doc.getString("windSpeedUnit") ?: "km/h"
    return UserPreferences(tempUnit, windSpeedUnit)
}
