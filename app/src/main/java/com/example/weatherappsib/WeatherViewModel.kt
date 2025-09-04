package com.example.weatherappsib

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

sealed interface WeatherUI {
    object Loading : WeatherUI
    data class Error(val message: String) : WeatherUI
    data class Content(
        val locationLabel: String,
        val temperatureC: String,
        val humidityPct: String,
        val windSpeed: String,
        val windDeg: Int,
        val windDirText: String,
        val cloudCoverPct: String,
        val precipMm: String
    ) : WeatherUI
}

class WeatherViewModel(
    private val repo: WeatherRepository
) : ViewModel() {

    private val _ui = MutableStateFlow<WeatherUI>(WeatherUI.Loading)
    val ui: StateFlow<WeatherUI> = _ui

    suspend fun reload(fused: FusedLocationProviderClient) {
        _ui.value = WeatherUI.Loading
        loadWithDeviceLocation(fused)
    }

    fun loadForMoscow() {
        viewModelScope.launch {
            _ui.value = WeatherUI.Loading
            val lat = 55.7558
            val lon = 37.6173
            val data = repo.fetch(lat, lon)
            _ui.value = data.toUI("Москва")
        }
    }

    @SuppressLint("MissingPermission")
    fun loadWithDeviceLocation(fused: FusedLocationProviderClient) {
        viewModelScope.launch {
            _ui.value = WeatherUI.Loading
            try {
                val cts = CancellationTokenSource()
                val loc: Location? = fused.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).awaitCancellable()
                if (loc != null) {
                    val data = repo.fetch(loc.latitude, loc.longitude)
                    val label = "Текущая локация (%.4f, %.4f)".format(loc.latitude, loc.longitude)
                    _ui.value = data.toUI(label)
                } else {
                    loadForMoscow()
                }
            } catch (t: Throwable) {
                _ui.value = WeatherUI.Error(t.message ?: "Неизвестная ошибка")
                loadForMoscow()
            }
        }
    }

    private fun WeatherPayload.toUI(label: String): WeatherUI.Content {
        val temp = temperatureC?.let { round1(it) }?.toString() ?: "—"
        val hum = humidityPct?.roundToInt()?.toString() ?: "—"
        val wsMs = windSpeedMs?.let { round1(it) }?.toString() ?: "—"
        val wd = windDirDeg?.roundToInt() ?: 0
        val wdText = windDirDeg?.let { windDirToText(it) } ?: "—"
        val cloud = cloudCoverPct?.roundToInt()?.toString() ?: "—"
        val prec = precipMm?.let { round1(it) }?.toString() ?: "—"

        return WeatherUI.Content(
            locationLabel = label,
            temperatureC = temp,
            humidityPct = hum,
            windSpeed = wsMs,
            windDeg = wd,
            windDirText = wdText,
            cloudCoverPct = cloud,
            precipMm = prec
        )
    }

    private fun round1(v: Double) = (v * 10.0).roundToInt() / 10.0

    private fun windDirToText(deg: Double): String {
        val dirs = listOf("С", "ССВ", "СВ", "ВСВ", "В", "ВЮВ", "ЮВ", "ЮЮВ", "Ю", "ЮЮЗ", "ЮЗ", "ЗЮЗ", "З", "ЗСЗ", "СЗ", "ССЗ")
        val idx = (((deg % 360 + 360) % 360) / 22.5).toInt()
        return dirs[idx % 16]
    }

    companion object {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WeatherViewModel(WeatherRepository(OpenMeteoService.create())) as T
            }
        }
    }
}

suspend fun <T> Task<T>.awaitCancellable(): T? =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { res -> if (cont.isActive) cont.resume(res) }
        addOnFailureListener { e -> if (cont.isActive) cont.resumeWithException(e) }
        addOnCanceledListener { if (cont.isActive) cont.resume(null) } // Task отменён -> вернуть null
        cont.invokeOnCancellation {
            // no-op: Task API не предоставляет отмену; дополнительные cleanup не требуются
        }
    }

