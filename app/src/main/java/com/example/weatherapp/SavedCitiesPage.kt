package com.example.weatherapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

@Composable
fun SavedCitiesPage(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Firestore reference to the user's document
    val userDocRef = firestore.collection("users").document(user?.uid ?: "")

    var savedCities by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    var mainCity by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var newCityName by remember { mutableStateOf(TextFieldValue("")) }

    // Fetch saved cities from Firestore
    LaunchedEffect(user?.uid) {
        try {
            userDocRef.get().await().let { document ->
                if (document.exists()) {
                    savedCities = document.get("savedCities") as? MutableMap<String, Boolean> ?: mutableMapOf()
                    mainCity = document.getString("mainCity") ?: "" // Ensure mainCity is fetched properly
                }
            }
        } catch (e: Exception) {
            Log.e("SavedCitiesPage", "Error fetching user data", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Saved Cities",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Refresh Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                // Refresh saved cities from Firestore
                refreshSavedCities(userDocRef, { newSavedCities, newMainCity ->
                    savedCities = newSavedCities
                    mainCity = newMainCity
                })
            }) {
                Text("Refresh", color = Color.White)
            }
        }

        // Display saved cities
        savedCities.forEach { (city, _) ->
            SettingSectionBox {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Navigate to SavedCityScreen with the selected city name
                            navController.navigate("SavedCityScreen/${city}")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = city,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    // Delete Button (Don't show if it's the main city)
                    if (city != mainCity) {
                        Button(
                            onClick = {
                                // Perform the delete action
                                deleteCity(userDocRef, city) {
                                    // After deleting, refresh cities
                                    refreshSavedCities(userDocRef) { newSavedCities, newMainCity ->
                                        savedCities = newSavedCities
                                        mainCity = newMainCity
                                    }
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { showDialog = true }) {
                Text("Add City", color = Color.White)
            }
        }
    }

    // Add City Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New City", color = Color.Black) },
            text = {
                OutlinedTextField(
                    value = newCityName,
                    onValueChange = { newCityName = it },
                    label = { Text("Enter city name", color = Color.Black) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCityName.text.isNotEmpty() && newCityName.text != mainCity) {
                            addCity(userDocRef, newCityName.text) {
                                // After adding the city, refresh cities
                                refreshSavedCities(userDocRef, { newSavedCities, newMainCity ->
                                    savedCities = newSavedCities
                                    mainCity = newMainCity
                                })
                            }
                            newCityName = TextFieldValue("") // Reset the input field
                            showDialog = false // Close dialog
                        }
                    }
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun SettingSectionBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)) // More rounded corners
            .background(Color(0xFF2D2D2D))
            .padding(16.dp)
    ) {
        content()
    }
}

fun refreshSavedCities(userDocRef: DocumentReference, onUpdated: (savedCities: MutableMap<String, Boolean>, mainCity: String) -> Unit) {
    userDocRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val updatedSavedCities = document.get("savedCities") as? MutableMap<String, Boolean> ?: mutableMapOf()
                val updatedMainCity = document.getString("mainCity") ?: ""
                onUpdated(updatedSavedCities, updatedMainCity)
            } else {
                Log.e("SavedCitiesPage", "Document not found")
            }
        }
        .addOnFailureListener { exception ->
            Log.e("SavedCitiesPage", "Error refreshing data: ${exception.message}")
        }
}


fun addCity(userDocRef: DocumentReference, city: String, onSuccess: () -> Unit) {
    val updates = mapOf("savedCities.$city" to true)

    userDocRef.update(updates)
        .addOnSuccessListener {
            Log.d("SavedCitiesPage", "City added successfully")
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Log.e("SavedCitiesPage", "Error adding city: ${exception.message}")
        }
}

fun deleteCity(userDocRef: DocumentReference, city: String, onSuccess: () -> Unit) {
    val updates = mapOf("savedCities.$city" to FieldValue.delete())

    userDocRef.update(updates)
        .addOnSuccessListener {
            Log.d("SavedCitiesPage", "City deleted successfully")
            onSuccess()
        }
        .addOnFailureListener { exception ->
            Log.e("SavedCitiesPage", "Error deleting city: ${exception.message}")
        }
}
