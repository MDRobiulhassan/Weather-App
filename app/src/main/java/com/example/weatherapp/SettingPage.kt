package com.example.weatherapp

import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.draw.clip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

@Composable
fun SettingPage(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Redirect to login if user is null
    LaunchedEffect(user) {
        if (user == null) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    // Prevent Firestore crash by ensuring user is not null
    val userDocRef: DocumentReference? = user?.uid?.let { firestore.collection("users").document(it) }

    if (user == null || userDocRef == null) {
        return
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var tempUnit by remember { mutableStateOf("Celsius") }
    var windSpeedUnit by remember { mutableStateOf("km/h") }
    var name by remember { mutableStateOf("John Doe") }
    val email = user?.email ?: "johndoe@example.com"
    var mainCity by remember { mutableStateOf("New York") }

    var showDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("") }
    var dialogValue by remember { mutableStateOf("") }

    // Fetch user settings from Firestore
    LaunchedEffect(user?.uid) {
        try {
            userDocRef.get().await().let { document ->
                if (document.exists()) {
                    notificationsEnabled = document.getBoolean("notifications") ?: true
                    tempUnit = document.getString("tempUnit") ?: "Celsius"
                    windSpeedUnit = document.getString("windSpeedUnit") ?: "km/h"
                    name = document.getString("name") ?: "John Doe"
                    mainCity = document.getString("mainCity") ?: "New York"
                }
            }
        } catch (e: Exception) {
            Log.e("SettingPage", "Error fetching user data", e)
        }
    }

    // Handle navigation after sign-out
    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser == null) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        SectionTitle("Weather Alerts")
        SectionBox {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { notificationsEnabled = !notificationsEnabled },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Units")
        SectionBox {
            SettingItem("Temperature", tempUnit, editable = true) {
                dialogType = "Temperature"
                dialogValue = tempUnit
                showDialog = true
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SectionBox {
            SettingItem("Wind Speed", windSpeedUnit, editable = true) {
                dialogType = "Wind Speed"
                dialogValue = windSpeedUnit
                showDialog = true
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("Account Control")
        SectionBox {
            SettingItem("Name", name) {
                dialogType = "Name"
                dialogValue = name
                showDialog = true
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SectionBox {
            SettingItem("Email", email, editable = false)
        }
        Spacer(modifier = Modifier.height(8.dp))
        SectionBox {
            SettingItem("Main City", mainCity) {
                dialogType = "Main City"
                dialogValue = mainCity
                showDialog = true
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                try {
                    auth.signOut()
                    Handler(Looper.getMainLooper()).post {
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SettingPage", "Error during sign-out", e)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }


    }

    if (showDialog) {
        EditDialog(
            title = "Edit $dialogType",
            value = dialogValue,
            onDismiss = { showDialog = false },
            onSave = { newValue ->
                when (dialogType) {
                    "Temperature" -> tempUnit = newValue
                    "Wind Speed" -> windSpeedUnit = newValue
                    "Name" -> name = newValue
                    "Main City" -> mainCity = newValue
                }
                saveSettings(userDocRef, notificationsEnabled, tempUnit, windSpeedUnit, name, mainCity)
                showDialog = false
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SectionBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // More rounded corners
            .background(Color(0xFF2D2D2D))
            .padding(16.dp)
            .height(55.dp) // Reduced height
    ) {
        content()
    }
}

@Composable
fun SettingItem(label: String, value: String, editable: Boolean = true, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = editable) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 18.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.White)
        Text(value, fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun EditDialog(
    title: String,
    value: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf(value) }
    var showOptions by remember { mutableStateOf(false) }

    val options: List<String> = when (title) {
        "Edit Temperature" -> listOf("Celsius", "Fahrenheit", "Kelvin")
        "Edit Wind Speed" -> listOf("km/h", "mph")
        else -> emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.Black) },
        text = {
            Column {
                if (options.isNotEmpty()) {
                    Text(
                        text = inputValue,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOptions = !showOptions }
                            .background(Color.White) // White background
                            .padding(16.dp)
                    )
                    if (showOptions) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White) // Dropdown box color is white
                        ) {
                            options.forEach { option ->
                                Text(
                                    text = option,
                                    color = Color.Black, // Text inside dropdown is black
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            inputValue = option
                                            showOptions = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Enter new value", color = Color.Black) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color(0xFFD3D3D3), // Grey input box
                            unfocusedContainerColor = Color(0xFFD3D3D3),
                            cursorColor = Color.Black,
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(inputValue)
                onDismiss()
            }) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

// Save user settings to Firestore
private fun saveSettings(
    userDocRef: DocumentReference,
    notificationsEnabled: Boolean,
    tempUnit: String,
    windSpeedUnit: String,
    name: String,
    mainCity: String
) {
    // Create a map with all the necessary fields
    val data = hashMapOf(
        "notifications" to notificationsEnabled,
        "tempUnit" to tempUnit,
        "windSpeedUnit" to windSpeedUnit,
        "name" to name,
        "mainCity" to mainCity
    )

    // Use set() with merge = true to only update changed fields, leaving others intact
    userDocRef.set(data, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("SettingPage", "Settings saved successfully")
        }
        .addOnFailureListener { e ->
            Log.e("SettingPage", "Error saving settings", e)
        }
}
