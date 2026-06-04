package com.example.lab9.presentation.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab9.presentation.viewmodel.LocationViewModel
import java.util.Date

@Composable
fun LocationScreen() {
    val context = LocalContext.current

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var permissionsGranted by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        permissionsGranted = permissionsMap.values.all { it }
        if (!permissionsGranted) {
            showSettingsDialog = true
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions)
    }

    if (showSettingsDialog && !permissionsGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Доступ до геолокації обов'язковий")
            Button(onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
                showSettingsDialog = false
            }) {
                Text("Відкрити налаштування")
            }
        }
        return
    }

    if (permissionsGranted) {
        val factory = com.example.lab9.presentation.viewmodel.LocationViewModelFactory(context.applicationContext as android.app.Application)
        val viewModel: LocationViewModel = viewModel(factory = factory)

        val locations by viewModel.locations.collectAsState()
        val status by viewModel.status.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Статус: $status",
                modifier = Modifier.padding(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.fetchAndSaveCurrentLocation() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отримати локацію")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Очистити")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            LazyColumn {
                items(locations) { location ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("Lat: ${location.latitude}, Lon: ${location.longitude}")
                            Text("Accuracy: ${location.accuracy ?: "n/a"}")
                            Text("Час: ${Date(location.timestamp)}")
                        }
                    }
                }
            }
        }
    }
}