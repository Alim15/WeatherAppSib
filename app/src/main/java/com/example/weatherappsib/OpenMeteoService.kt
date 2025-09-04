package com.example.weatherappsib


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {

    // Используем "current=" — возвращает сразу текущие значения по запрошенным параметрам.
    // Дополнительно запрашиваем "timezone=auto" для корректной привязки.
    @GET("v1/forecast")
    suspend fun getCurrent(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,precipitation,cloud_cover,wind_speed_10m,wind_direction_10m",
        @Query("timezone") tz: String = "auto",
        // Опционально: единицы измерений — скорость ветра в м/с, осадки в мм, температура в °C
        @Query("wind_speed_unit") wsUnit: String = "ms"
    ): OpenMeteoResponse

    companion object {
        fun create(): OpenMeteoService {
            val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            val client = OkHttpClient.Builder()
                .addInterceptor(log)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build()

            return retrofit.create(OpenMeteoService::class.java)
        }
    }
}

data class OpenMeteoResponse(
    val latitude: Double?,
    val longitude: Double?,
    val current: CurrentBlock?
)

data class CurrentBlock(
    // Таймстемп может понадобиться для отладки/вывода
    val time: String?,
    val temperature_2m: Double?,
    val relative_humidity_2m: Double?,
    val precipitation: Double?,
    val cloud_cover: Double?,
    val wind_speed_10m: Double?,
    val wind_direction_10m: Double?
)