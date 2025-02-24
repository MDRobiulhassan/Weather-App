package com.example.weatherapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun getCityFromFirebase(): String {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    return try {
        val snapshot = db.collection("users")
            .document(currentUser?.uid ?: "")
            .get()
            .await()
        snapshot.getString("mainCity") ?: "Unknown"
    } catch (e: Exception) {
        Log.e("Firebase", "Error fetching city", e)
        "Unknown"
    }
}