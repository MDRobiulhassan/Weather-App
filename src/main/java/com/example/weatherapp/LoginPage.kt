package com.example.weatherapp

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginPage(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                text = "Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Email Input
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        // Check if user has set mainCity before navigating
                                        checkUserMainCity(userId, db, navController, context)
                                        scheduleNotifications(context) // <-- PLACE IT HERE
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Login failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Navigation
            Text(
                text = "Don't Have an Account?",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SIGN UP",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        navController.navigate("signup")  // Navigate to the signup page
                    }
                    .padding(top = 8.dp)
            )
        }
    }
}

fun checkUserMainCity(userId: String, db: FirebaseFirestore, navController: NavHostController, context: Context) {
    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            // Fetching mainCity, checking for null or empty string
            val mainCity = document.getString("mainCity")
            if (mainCity.isNullOrEmpty()) {
                // If mainCity is empty or null, navigate to LocationScreen
                Toast.makeText(context, "Setting location", Toast.LENGTH_SHORT).show()
                navController.navigate("location")  // Navigate to location screen
            } else {
                // If mainCity is set, navigate to HomeScreen
                Toast.makeText(context, "Navigating to Home", Toast.LENGTH_SHORT).show()
                navController.navigate("home")  // Navigate to home screen
            }
        }
        .addOnFailureListener { exception ->
            // Logging the error and showing a toast if fetching data failed
            exception.printStackTrace()
            Toast.makeText(context, "Error fetching user data", Toast.LENGTH_SHORT).show()
        }
}
