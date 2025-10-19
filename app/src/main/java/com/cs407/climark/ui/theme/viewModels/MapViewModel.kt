package com.cs407.climark.ui.theme.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MapState(
// A list of markers currently displayed on the map
    val markers: List<LatLng> = emptyList(),
// Stores the user's most recent location (if available)
    val currentLocation: LatLng? = null,
// Tracks whether location permissions are granted
    val locationPermissionGranted: Boolean = false,
// Indicates when location or map data is being loaded
    val isLoading: Boolean = false,
// Stores any error message encountered
    val error: String? = null
)

class MapViewModel : ViewModel() {
    // Backing property (private) for state: MutableStateFlow allows us
// to update data internally
    private val _uiState = MutableStateFlow(MapState())
    // Publicly exposed immutable StateFlow for the UI layer to observe changes safely
    val uiState = _uiState.asStateFlow()
    // FusedLocationProviderClient interacts with Android's location services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Initializes the location client when a valid Context becomes available
    fun initializeLocationClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }
    // Coroutine function to fetch the user's current location
    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
// TODO: 1 - Set isLoading to true and clear previous errors
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
// TODO: 2 - Retrieve the last known location using fusedLocationClient
                val location = fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .await()
// TODO: 3 - Handle cases where location is null (set an appropriate error message)
                if (location == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "Unable to retrieve location.",
                        isLoading = false
                    )
                } else {
                    // TODO: 4 - If successful, update currentLocation with latitude and longitude
                    val latLng = LatLng(location.latitude, location.longitude)
                    _uiState.value = _uiState.value.copy(
                        currentLocation = latLng,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error fetching location: ${e.message}",
                    isLoading = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
// TODO: 5 - Always set isLoading back to false when done
// TODO: 6 - Wrap logic inside try-catch to handle possible exceptions
        }
    }
    // Updates permission flag when the user grants or denies location access
    fun updateLocationPermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(locationPermissionGranted = granted)
    }
}