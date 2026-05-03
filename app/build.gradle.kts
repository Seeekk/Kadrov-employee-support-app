plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.ozhd.kadrov.employeesupport"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.ozhd.kadrov.employeesupport"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Базовый URL REST API (замените на адрес вашего бэкенда).
        buildConfigField("String", "API_BASE_URL", "\"https://api.example.com/\"")
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.viewpager2)
    implementation(libs.swiperefresh)
    implementation(libs.recyclerview)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.common.java8)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.splashscreen)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
