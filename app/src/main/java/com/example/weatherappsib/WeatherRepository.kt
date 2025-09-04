package com.example.weatherappsib

class WeatherRepository(
    private val api: OpenMeteoService
) {
    suspend fun fetch(lat: Double, lon: Double): WeatherPayload {
        val r = api.getCurrent(lat, lon)
        val c = r.current
        return WeatherPayload(
            temperatureC = c?.temperature_2m,
            humidityPct = c?.relative_humidity_2m,
            windSpeedMs = c?.wind_speed_10m,
            windDirDeg = c?.wind_direction_10m,
            cloudCoverPct = c?.cloud_cover,
            precipMm = c?.precipitation
        )
    }
}

data class WeatherPayload(
    val temperatureC: Double?,
    val humidityPct: Double?,
    val windSpeedMs: Double?,
    val windDirDeg: Double?,
    val cloudCoverPct: Double?,
    val precipMm: Double?
)