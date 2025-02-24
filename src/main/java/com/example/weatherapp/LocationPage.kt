package com.example.weatherapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun LocationPage(navController: NavController) {
    val context = LocalContext.current
    var locationPermissionGranted by remember { mutableStateOf<Boolean?>(null) }
    var city by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var citySuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    // Handle the location permission request
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean -> locationPermissionGranted = isGranted }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EE))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Location",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (locationPermissionGranted) {
                null -> {
                    Text(
                        text = "We need access to your location. Would you like to grant permission?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Text(text = "Yes", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                locationPermissionGranted = false
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                        ) {
                            Text(text = "No", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                false -> {
                    // Show city input field if location access is not granted
                    Text(
                        text = "Please enter your city:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // City TextField
                    TextField(
                        value = city,
                        onValueChange = {
                            city = it
                            citySuggestions = getCitySuggestions(it)
                        },
                        label = { Text("City", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null
                    )

                    // Show error if any
                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Display suggested cities as a clickable list
                    if (citySuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            citySuggestions.forEach { suggestedCity ->
                                Text(
                                    text = suggestedCity,
                                    color = Color.Blue,
                                    modifier = Modifier
                                        .clickable {
                                            city = suggestedCity
                                            errorMessage = null // Clear any previous errors
                                        }
                                        .padding(4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Save Button
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (city.isEmpty()) {
                                errorMessage = "City cannot be empty!"
                            } else {
                                // Save the city to Firestore
                                saveCityToUserProfile(city, navController, context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6200EE),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "SAVE", fontWeight = FontWeight.Bold)
                    }
                }

                else -> {
                    // Handle location access if granted
                    getLastLocationAndSaveCity(context, navController)
                }
            }
        }
    }
}

fun getCitySuggestions(input: String): List<String> {
    val allCities = listOf("New York", "Los Angeles", "San Francisco", "Miami", "Chicago", "Dallas", "Seattle", "Austin", "Boston", "Denver")
    return allCities.filter { it.contains(input, ignoreCase = true) }
}

fun getCityFromCoordinates(context: Context, latitude: Double, longitude: Double): String? {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        val address = addresses?.firstOrNull()
        if (address != null) {
            val city = address.locality
            val subLocality = address.subLocality
            val country = address.countryName

            // Log or print the address components to debug
            Log.d("Location", "City: $city, Sub-locality: $subLocality, Country: $country")

            // Choose the most accurate component
            city ?: subLocality ?: country
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getLastLocationAndSaveCity(context: Context, navController: NavController) {
    // Check for permissions before accessing the location
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val cityFromCoordinates = getCityFromCoordinates(context, latitude, longitude)

                    if (cityFromCoordinates != null) {
                        // Save the city name in Firestore
                        saveCityToUserProfile(cityFromCoordinates, navController, context)
                    } else {
                        // Handle case where city could not be found
                        Toast.makeText(context, "Unable to determine the city", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Handle case where location is null
                    Toast.makeText(context, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        // Request permission if not granted
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1
        )
    }
}

fun saveCityToUserProfile(city: String, navController: NavController, context: Context) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        val userId = user.uid // Get the current authenticated user's UID
        val db = FirebaseFirestore.getInstance()

        // Fetch the current document of the user
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Get the current data of the user document
                    val currentData = documentSnapshot.data?.toMutableMap() ?: mutableMapOf()

                    // Update the mainCity field
                    currentData["mainCity"] = city

                    // Update the savedCities field (add the city if it's not already present)
                    val savedCities = (currentData["savedCities"] as? MutableMap<String, Boolean>) ?: mutableMapOf()
                    savedCities[city] = true
                    currentData["savedCities"] = savedCities

                    // Now, update the document with the modified data
                    db.collection("users").document(userId)
                        .update(currentData) // Update only the relevant fields
                        .addOnSuccessListener {
                            Toast.makeText(context, "City saved: $city", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") // Navigate to the next screen after saving
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Error saving city: ${e.message}")
                            Toast.makeText(context, "Error saving city: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Handle the case where the document doesn't exist
                    Toast.makeText(context, "User document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error fetching user document: ${e.message}")
                Toast.makeText(context, "Error fetching user document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    } else {
        Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
    }
}





