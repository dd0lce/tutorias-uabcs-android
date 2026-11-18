import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// `api.url` en local.properties (no se sube a Git). Sin él, la app usa el emulador: 10.0.2.2 es tu computadora.
val local = Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load) }
val apiUrl: String = local.getProperty("api.url", "http://10.0.2.2:8081")

android {
    namespace = "mx.uabcs.tutorias"
    compileSdk = 37

    defaultConfig {
        applicationId = "mx.uabcs.tutorias"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "BASE_URL", "\"$apiUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.navigation.suite)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    // El org.json de Android es un stub en las pruebas locales.
    testImplementation(libs.json)
}
