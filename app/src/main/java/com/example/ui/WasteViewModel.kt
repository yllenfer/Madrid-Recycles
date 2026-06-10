package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GarbageSortingResult
import com.example.data.GeminiClient
import com.example.data.MadridData
import com.example.data.MadridPuntoLimpio
import com.example.data.WasteContainer
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(val result: GarbageSortingResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

data class PuntoLimpioDistance(
    val punto: MadridPuntoLimpio,
    val distanceKm: Double
)

class WasteViewModel : ViewModel() {
    private val TAG = "WasteViewModel"

    // Core states
    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _userLatitude = MutableStateFlow(MadridData.MADRID_CENTER_LAT)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(MadridData.MADRID_CENTER_LNG)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    private val _sortedPuntosLimpios = MutableStateFlow<List<PuntoLimpioDistance>>(emptyList())
    val sortedPuntosLimpios: StateFlow<List<PuntoLimpioDistance>> = _sortedPuntosLimpios.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isCustomLocationMocked = MutableStateFlow(true) // Defaults to Puerta del Sol mock for easy simulator demo
    val isCustomLocationMocked: StateFlow<Boolean> = _isCustomLocationMocked.asStateFlow()

    private val _language = MutableStateFlow("es") // "es" or "en"
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        // Initialize points with default Madrid center location
        recalculateDistances()
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == "es") "en" else "es"
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearScanState() {
        _scanState.value = ScanUiState.Idle
    }

    // Recalculates distances from the current coordinates
    private fun recalculateDistances() {
        val lat = _userLatitude.value
        val lng = _userLongitude.value
        
        val list = MadridData.puntosLimpios.map { punto ->
            val dist = calculateDistanceInKm(lat, lng, punto.latitude, punto.longitude)
            PuntoLimpioDistance(punto, dist)
        }.sortedBy { it.distanceKm }
        
        _sortedPuntosLimpios.value = list
    }

    // Query coordinates from GPS if permissions are granted
    fun requestDeviceLocation(context: Context) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        _userLatitude.value = location.latitude
                        _userLongitude.value = location.longitude
                        _isCustomLocationMocked.value = false
                        recalculateDistances()
                        Log.d(TAG, "Successfully updated current location: ${location.latitude}, ${location.longitude}")
                    } else {
                        Log.d(TAG, "Last location was null, keeping default Madrid center.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed obtaining location. Fallback active: ${e.message}")
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing location permissions: ${e.message}")
        }
    }

    // Set custom demo location (e.g. simulation around Madrid center districts)
    fun setDemoLocation(districtName: String, lat: Double, lng: Double) {
        _userLatitude.value = lat
        _userLongitude.value = lng
        _isCustomLocationMocked.value = true
        recalculateDistances()
        Log.d(TAG, "Theme location mocked to $districtName: $lat, $lng")
    }

    // Triggers text search on any object
    fun searchWaste(query: String) {
        if (query.trim().isEmpty()) return
        
        _scanState.value = ScanUiState.Loading
        val lang = _language.value
        viewModelScope.launch {
            try {
                val result = GeminiClient.analyzeSearchQuery(query, lang)
                if (result != null) {
                    _scanState.value = ScanUiState.Success(result)
                } else {
                    _scanState.value = ScanUiState.Error(AppStrings.get("error_generic", lang))
                }
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(AppStrings.get("error_conn", lang) + e.localizedMessage)
            }
        }
    }

    // Triggers analysis based on base64 captured bitmap
    fun scanImage(bitmap: Bitmap) {
        _scanState.value = ScanUiState.Loading
        val lang = _language.value
        viewModelScope.launch {
            try {
                val result = GeminiClient.analyzeImageFrame(bitmap, lang)
                if (result != null) {
                    _scanState.value = ScanUiState.Success(result)
                } else {
                    _scanState.value = ScanUiState.Error(AppStrings.get("error_image", lang))
                }
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error(AppStrings.get("error_image_proc", lang) + e.localizedMessage)
            }
        }
    }

    // Haversine formula to compute distance in Km cleanly between coordinates
    private fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
