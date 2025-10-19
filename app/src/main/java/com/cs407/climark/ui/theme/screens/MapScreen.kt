package com.cs407.climark.ui.theme.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.climark.ui.theme.viewModels.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    // Observe the current UI state from the ViewModel.
// This automatically updates the UI whenever data changes.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
// TODO: Ask for location permission if not already granted.
// [HINT] You can use a helper composable like LocationPermissionHelper
// and call viewModel.updateLocationPermission(granted) when permission is approved.
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() //for FAB
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Update ViewModel when permission is granted or denied
        viewModel.updateLocationPermission(granted)
        if (granted) {
            viewModel.initializeLocationClient(context)
            viewModel.getCurrentLocation()
        }
    }

// Check if permission is already granted
    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

// Launch the permission request if not granted
    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.updateLocationPermission(true)
            viewModel.initializeLocationClient(context)
            viewModel.getCurrentLocation()
        }
    }
// Define a default (fallback) location, for example the UW-Madison campus.
// This ensures that the map loads to a valid position even before
// the user's actual location is obtained.
    val defaultLocation = LatLng(43.0731, -89.4012) // Madison, WI
// Create a camera state to control and remember the map's current viewpoint.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }
// Automatically move (animate) the camera when the location changes.
// LaunchedEffect runs a coroutine whenever uiState.currentLocation updates.
    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let { location ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                location, 15f // 15f = street-level zoom
            )
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Display the Google Map on screen
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
// TODO Display the marker only when the current location is available
            uiState.currentLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Your Location"
                )
            }
        }

        val yourLocation = uiState.currentLocation
        FloatingActionButton(
            modifier = Modifier
                .padding(10.dp)
                .size(60.dp),
            onClick = {
                yourLocation?.let { location ->
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                location,
                                15f
                            )
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Your Location",
                modifier = Modifier.size(50.dp)
            )
        }
    }
}
