package com.example.weatherappsib

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var fused: FusedLocationProviderClient
    private val vm: WeatherViewModel by viewModels { WeatherViewModel.factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fused = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WeatherScreen(vm = vm, fused = fused)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(vm: WeatherViewModel, fused: FusedLocationProviderClient) {
    val scope = rememberCoroutineScope()
    val ui by vm.ui.collectAsState()

    val finePerm = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarsePerm = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)

    LaunchedEffect(Unit) {
        // Пытаемся запросить разрешения сразу; если не дадут — будет автоподстановка Москвы
        if (!finePerm.status.isGranted && !coarsePerm.status.isGranted) {
            finePerm.launchPermissionRequest()
        } else {
            vm.loadWithDeviceLocation(fused)
        }
    }

    // Если пользователь только что дал доступ — пробуем ещё раз
    LaunchedEffect(finePerm.status.isGranted, coarsePerm.status.isGranted) {
        if (finePerm.status.isGranted || coarsePerm.status.isGranted) {
            vm.loadWithDeviceLocation(fused)
        } else {
            vm.loadForMoscow()
        }
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        when (val s = ui) {
            is WeatherUI.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            is WeatherUI.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Ошибка: ${s.message}", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { scope.launch { vm.reload(fused) } }) { Text("Повторить") }
                }
            }
            is WeatherUI.Content -> {
                WeatherCard(s)
            }
        }
    }
}

@Composable
fun WeatherCard(content: WeatherUI.Content) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = content.locationLabel,
                style = MaterialTheme.typography.titleLarge
            )
            Text("Температура: ${content.temperatureC} °C")
            Text("Влажность: ${content.humidityPct} %")
            Text("Ветер: ${content.windDirText} ${content.windSpeed} м/с (${content.windDeg}°)")
            Text("Облачность: ${content.cloudCoverPct} %")
            Text("Осадки: ${content.precipMm} мм")
            Text(
                text = "Источник: Open-Meteo",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}