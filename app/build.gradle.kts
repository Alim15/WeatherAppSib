import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.weatherappsib"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.weatherappsib"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += setOf("/META-INF/AL2.0", "/META-INF/LGPL2.1")
        }
    }
}

dependencies {

    implementation ("androidx.compose:compose-bom:2025.01.00")
    implementation ("androidx.activity:activity-compose:1.9.2")
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-tooling-preview")
    debugImplementation ("androidx.compose.ui:ui-tooling")
    implementation ("androidx.compose.material3:material3:1.3.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Location
    implementation ("com.google.android.gms:play-services-location:21.3.0")

    // Networking
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Accompanist permissions (упрощает runtime-доступы в Compose)
    implementation ("com.google.accompanist:accompanist-permissions:0.36.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}