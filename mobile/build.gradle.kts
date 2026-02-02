plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 1. ПРИМЕНЯЕМ ПЛАГИН
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.pacecomplication"
    compileSdk = 36
    buildFeatures {
        compose = true // Это говорит компилятору: "Мы работаем в Compose"
    }

    defaultConfig {
        applicationId = "com.example.pacecomplication"
        minSdk = 30
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)

    // Вот то, что ты пытаешься импортировать
    implementation(libs.androidx.material3)

    // И базовые вещи для отрисовки
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.location)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.android)
    implementation(libs.play.services.location.v2110)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material)
    testImplementation(libs.junit)
    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.media:media:1.7.1")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
    wearApp(project(":wear"))
}