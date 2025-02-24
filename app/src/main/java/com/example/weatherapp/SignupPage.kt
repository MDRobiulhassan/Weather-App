package com.example.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupPage(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showErrorAlert by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    if (showErrorAlert) {
        AlertDialog(
            onDismissRequest = { showErrorAlert = false },
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage, fontWeight = FontWeight.Bold) },
            confirmButton = {
                TextButton(onClick = { showErrorAlert = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

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
                text = "Sign Up",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", fontWeight = FontWeight.Bold) },
                isError = nameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", fontWeight = FontWeight.Bold) },
                isError = emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontWeight = FontWeight.Bold) },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", fontWeight = FontWeight.Bold) },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    nameError = name.isEmpty()
                    emailError = email.isEmpty()
                    passwordError = password.isEmpty() || password.length < 8
                    confirmPasswordError = confirmPassword.isEmpty() || confirmPassword != password

                    errorMessage = when {
                        nameError -> "Name cannot be empty"
                        emailError -> "Email cannot be empty"
                        passwordError -> "Password must be at least 8 characters"
                        confirmPasswordError -> "Passwords do not match"
                        else -> ""
                    }

                    if (errorMessage.isNotEmpty()) {
                        showErrorAlert = true
                        return@Button
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val userId = user?.uid

                                val userData = hashMapOf(
                                    "userId" to userId,
                                    "name" to name,
                                    "email" to email,
                                    "notifications" to true,
                                    "tempUnit" to "Celsius",
                                    "windSpeedUnit" to "km/h",
                                    "mainCity" to "",
                                    "savedCities" to mapOf<String, String>()
                                )

                                if (userId != null) {
                                    firestore.collection("users").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            navController.navigate("login")
                                        }
                                        .addOnFailureListener {
                                            errorMessage = "Failed to save user data"
                                            showErrorAlert = true
                                        }
                                }
                            } else {
                                errorMessage = "Signup failed: ${task.exception?.message}"
                                showErrorAlert = true
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White
                )
            ) {
                Text(text = "SIGN UP", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Already have an account?",
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LOGIN",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { navController.navigate("login") }
                    .padding(top = 8.dp)
            )
        }
    }
}
