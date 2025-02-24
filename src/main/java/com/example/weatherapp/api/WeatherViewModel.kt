package com.example.weatherapp.api


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.WeatherResponse
import com.example.weatherapp.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData

    fun fetchWeather(cityName: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getWeatherData(
                    apiKey = "06c121bc7bec4a3ca01141936250502",
                    cityName = cityName
                )
                _weatherData.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
