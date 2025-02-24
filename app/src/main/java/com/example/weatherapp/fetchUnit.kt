package com.example.weatherapp

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Function to fetch the temperature unit from Firestore
suspend fun fetchUnit(userId: String): String {
    val db = FirebaseFirestore.getInstance()

    return try {
        val document = db.collection("users").document(userId).get().await()

        // Assuming the temperature unit is stored in a field called "tempUnit"
        val tempUnit = document.getString("tempUnit") ?: "Celsius" // Default to Celsius if not found
        tempUnit
    } catch (e: Exception) {
        // Handle error if fetch fails
        e.printStackTrace()
        "Celsius" // Default to Celsius if error occurs
    }
}
