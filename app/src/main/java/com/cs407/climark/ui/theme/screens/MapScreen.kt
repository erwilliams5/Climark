package com.cs407.climark.ui.theme.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {
// Defines a state object that controls the camera's position on the map
    val initLocation = LatLng(1.35, 103.87) // Hardcoded coordinates (Singapore)
    val cameraPositionState = rememberCameraPositionState {
// Sets the initial position and zoom level of the map
        position = CameraPosition.fromLatLngZoom(initLocation, 12f)
    }
// The GoogleMap composable displays the map UI inside your Compose layout
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
}