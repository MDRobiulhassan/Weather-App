package com.example.weatherapp


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun SavedCityScreen(navController: NavHostController, cityName: String) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val userId = firebaseAuth.currentUser?.uid ?: ""

    val scrollState = rememberScrollState()
    var weather by remember { mutableStateOf(Weather("", "Unknown", "", "")) }
    var tempUnit by remember { mutableStateOf("Celsius") }

    // Fetch weather data based on the passed city name
    LaunchedEffect(cityName) {
        if (cityName.isNotEmpty()) {
            tempUnit = fetchUnit(userId)  // Fetch temperature unit from Firestore
            weather = fetchWeather(cityName, tempUnit)  // Fetch weather using the passed city name
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background based on weather condition
        val backgroundImage = when {
            weather.condition.contains("clear", ignoreCase = true) -> R.drawable.clear
            weather.condition.contains("sunny", ignoreCase = true) -> R.drawable.sunny
            weather.condition.contains("snow", ignoreCase = true) -> R.drawable.snow
            weather.condition.contains("rain", ignoreCase = true) -> R.drawable.rainy
            weather.condition.contains("fog", ignoreCase = true) -> R.drawable.fog
            weather.condition.contains("thunderstorm", ignoreCase = true) -> R.drawable.thunderstorm
            weather.condition.contains("haze", ignoreCase = true) -> R.drawable.fog
            weather.condition.contains("mist", ignoreCase = true) -> R.drawable.fog
            weather.condition.equals("cloudy", ignoreCase = true) -> R.drawable.cloudy
            weather.condition.equals("cloud", ignoreCase = true) -> R.drawable.cloudy
            weather.condition.equals("partly cloudy", ignoreCase = true) -> R.drawable.cloudy
            else -> R.drawable.clear
        }

        // Set background image
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay a dark transparent layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Column for all your content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Top Section: City Name and Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { navController.navigate("savedcities") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.citylist),
                            contentDescription = "City List",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { navController.navigate("setting") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.setting),
                            contentDescription = "Setting",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // "Back to Main City" Text just below the city name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 1.dp) // Padding below city name
                    .zIndex(1f)  // Place it on top of city name
            ) {
                Text(
                    text = "Back to Main City >",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .clickable {
                            // Ensure "homepage" exists in your NavHost setup
                            navController.navigate("home")
                        }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Middle Section: Temperature and Weather Condition
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rawTemp = weather.temp.trim()
                Log.d("WeatherDebug", "Raw temperature from API: $rawTemp")

                // Parse the raw temperature from the API response
                val tempCelsius = rawTemp.toDoubleOrNull() ?: Double.NaN
                Log.d("WeatherDebug", "Parsed Celsius temperature: $tempCelsius")

                // Convert to the correct temperature based on the selected unit (Celsius/Fahrenheit)
                val temperature = when (tempUnit) {
                    "Celsius" -> {
                        if (!tempCelsius.isNaN()) {
                            "${tempCelsius.toInt()}°C"
                        } else {
                            "N/A"
                        }
                    }
                    else -> {  // Fahrenheit case
                        if (!tempCelsius.isNaN()) {
                            // Convert Celsius to Fahrenheit using the formula
                            val tempFahrenheit = ((weather.minTemp.toDoubleOrNull() ?: 0.0) + (weather.maxTemp.toDoubleOrNull() ?: 0.0)) / 2
                            Log.d("WeatherDebug", "Converted Fahrenheit temperature: $tempFahrenheit")

                            // Round the Fahrenheit value after conversion
                            val roundedFahrenheit = tempFahrenheit.toInt()
                            Log.d("WeatherDebug", "Rounded Fahrenheit: $roundedFahrenheit")

                            "${roundedFahrenheit}°F"
                        } else {
                            "N/A"
                        }
                    }
                }

                // Display the temperature value
                Text(
                    text = temperature,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // Weather Condition (Corrected min/max temp display as well)
                val minTemp = weather.minTemp.toDoubleOrNull()?.toInt() ?: "N/A"
                val maxTemp = weather.maxTemp.toDoubleOrNull()?.toInt() ?: "N/A"

                Text(
                    text = "${weather.condition}   $minTemp°/$maxTemp°",  // Simplified string interpolation
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Hourly Weather Forecast Section
            SavedCityHourlyForecast(cityName, userId)

            Spacer(modifier = Modifier.height(32.dp))

            // 7 Days Daily Weather Forecast Section
            SavedDailyWeatherForecast(navController,cityName, userId)

            Spacer(modifier = Modifier.height(32.dp))

            // Weather Stats Section
            SavedWeatherStats(cityName, userId)

            Spacer(modifier = Modifier.height(32.dp))

            // Sunset and Sunrise Section in a Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(16.dp)
            ) {
                SavedSunsetSunrise(cityName, userId)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}



@Composable
fun SavedCityHourlyForecast(cityName:String,userId: String) {
    var tempUnit by remember { mutableStateOf("Celsius") }
    var hourlyWeatherList by remember { mutableStateOf<List<HourlyWeather>>(emptyList()) }


    LaunchedEffect(userId) {
        tempUnit = fetchUnit(userId)
        val city = cityName
        hourlyWeatherList = fetchHourlyWeather(city, tempUnit)
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hourly Forecast",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(hourlyWeatherList) { hourlyWeather ->
                HourlyWeatherItem(
                    time = hourlyWeather.time,
                    iconRes = SavedgetWeatherIcon(hourlyWeather.condition),
                    temperature = hourlyWeather.temperature,
                    tempUnit = tempUnit // Pass the tempUnit here
                )
            }
        }
    }
}


@Composable
fun SavedHourlyWeatherItem(time: String, iconRes: Int, temperature: String, tempUnit: String) {
    val displayTime = SavedextractTimeFromDate(time) // Extract only the time
    val displayTemp = SavedformatTemperature(temperature, tempUnit) // Format the temperature
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = displayTime, // Use the extracted time
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Weather Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayTemp, // Use the formatted temperature
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}


// Function to return the appropriate weather icon based on the condition
fun SavedgetWeatherIcon(condition: String): Int {
    val lowerCaseCondition = condition.lowercase() // Convert to lowercase for case-insensitive matching


    return when {
        "clear" in lowerCaseCondition -> R.drawable.clearicon
        "cloud" in lowerCaseCondition -> R.drawable.cloudicon
        "fog" in lowerCaseCondition || "mist" in lowerCaseCondition -> R.drawable.fogicon
        "rain" in lowerCaseCondition || "drizzle" in lowerCaseCondition -> R.drawable.raincon
        "snow" in lowerCaseCondition || "sleet" in lowerCaseCondition -> R.drawable.snowicon
        "thunder" in lowerCaseCondition || "storm" in lowerCaseCondition -> R.drawable.thunderstormicon
        else -> R.drawable.clearicon // Default icon
    }
}



// Function to fetch hourly weather data
suspend fun SavedfetchWeatherData(userId: String,cityName: String): List<HourlyWeather> {
    val tempUnit = fetchUnit(userId)
    val city = cityName
    return fetchHourlyWeather(city, tempUnit)
}


fun SavedextractTimeFromDate(dateString: String): String {
    return try {
        // Check if the dateString is null or empty
        if (dateString.isBlank()) {
            return "N/A" // Return a placeholder if the date string is invalid
        }


        // Define the input and output formats
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())


        // Parse the date string and format it to extract only the time
        val date = inputFormat.parse(dateString)
        outputFormat.format(date)
    } catch (e: Exception) {
        // Log the error for debugging
        e.printStackTrace()
        "N/A" // Return a placeholder if parsing fails
    }
}


fun SavedformatTemperature(temperature: String, tempUnit: String): String {
    return try {
        val tempValue = temperature.replace("°C", "").replace("°F", "").toDouble()
        val roundedTemp = tempValue.toInt() // Round to the nearest integer
        "$roundedTemp°${if (tempUnit == "Celsius") "C" else "F"}" // Append the unit
    } catch (e: Exception) {
        temperature // Fallback to the original string if parsing fails
    }
}

@Composable
fun SavedDailyWeatherForecast(navController: NavController, cityName: String, userId: String) {
    var dailyWeatherList by remember { mutableStateOf<List<DailyWeather>>(emptyList()) }
    var tempUnit by remember { mutableStateOf("Celsius") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch data when the component is launched
    LaunchedEffect(Unit) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            tempUnit = fetchUnit(userId) // Fetch temperature unit from Firestore
            dailyWeatherList = fetch7DayForecast(cityName, tempUnit) // Fetch weather for the given city
        } catch (e: Exception) {
            errorMessage = "Failed to load weather data: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "7-Day Forecast",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
        }

        if (!isLoading && dailyWeatherList.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dailyWeatherList.forEach { dailyWeather ->
                    SavedDailyWeatherItem(
                        date = dailyWeather.date,
                        iconRes = getWeatherIcon(dailyWeather.condition),
                        temperature = SavedformatTemperature(dailyWeather.temperature, tempUnit),
                        onClick = {
                            navController.navigate("daily_details/${cityName}/${dailyWeather.date}")
                        }
                    )
                }
            }
        }
    }
}





fun SavedformatTemperature(temperature: Double, unit: String): String {
    return if (unit == "Fahrenheit") {
        String.format("%.0f°F", temperature)
    } else {
        String.format("%.0f°C", temperature)
    }
}






@Composable
fun SavedDailyWeatherItem(date: String, iconRes: Int, temperature: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { onClick() } // ✅ Make row clickable
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Weather Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = temperature,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}



@Composable
fun SavedWeatherStats(cityName:String,userId: String) {
    var weatherStats by remember { mutableStateOf<WeatherStatsData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current


    // Fetch user preferences and weather data when the component is launched
    LaunchedEffect(Unit) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val preferences = getUserPreferences(userId) // Fetch user preferences
            val city = cityName // Call the function without arguments
            weatherStats = fetchWeatherStats(city, preferences) // Fetch weather stats from API
        } catch (e: Exception) {
            errorMessage = "Failed to load weather data: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weather Stats",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }


        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
        }


        if (!isLoading && weatherStats != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SavedWeatherStatBox(
                    title = "Feels Like",
                    value = "${weatherStats?.feelsLike}",  // No need to append °C or °F here, it's already part of the value
                    iconRes = R.drawable.feelslikeicon
                )
                SavedWeatherStatBox(
                    title = "Humidity",
                    value = "${weatherStats?.humidity}%", // Display humidity as percentage
                    iconRes = R.drawable.humidityicon
                )
                SavedWeatherStatBox(
                    title = "Wind",
                    value = "${weatherStats?.windSpeed}", // No need to append kph or mph here, it's already part of the value
                    iconRes = R.drawable.windicon
                )
            }
        }
    }
}






@Composable
fun SavedWeatherStatBox(title: String, value: String, iconRes: Int) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun SavedSunsetSunrise(cityName:String,userId: String) {
    // A state to hold the fetched sunrise and sunset times
    val sunsetSunriseData = remember { mutableStateOf<SunsetSunriseData?>(null) }


    // A state to hold the city name fetched from Firebase
    val city = remember { mutableStateOf("") }


    // Fetch the city name from Firebase in a coroutine
    LaunchedEffect(Unit) {
        city.value = cityName  // Get the user's main city from Firebase asynchronously
    }


    // Fetch the sunrise and sunset times when the city value changes
    LaunchedEffect(city.value) {
        if (city.value.isNotEmpty()) {
            sunsetSunriseData.value = fetchSunsetSunrise(city.value)  // Use the main city to fetch sunrise and sunset
        }
    }


    val sunrise = sunsetSunriseData.value?.sunrise ?: "Loading..."
    val sunset = sunsetSunriseData.value?.sunset ?: "Loading..."


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sunset & Sunrise",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sunrise Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.sunriseicon),
                    contentDescription = "Sunrise",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sunrise",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = sunrise,  // Dynamically show sunrise time
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }


            // Sunset Section
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.sunseticon),
                    contentDescription = "Sunset",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sunset",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = sunset,  // Dynamically show sunset time
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

