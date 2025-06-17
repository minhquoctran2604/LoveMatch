plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "vn.edu.tlu.cse.lovematch"
    compileSdk = 35

    defaultConfig {
        applicationId = "vn.edu.tlu.cse.lovematch"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"YOUR_GEMINI_API_KEY_HERE\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GEMINI_API_KEY", "\"YOUR_GEMINI_API_KEY_HERE\"")
        }
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"YOUR_GEMINI_API_KEY_HERE\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Core Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // UI Components
    implementation(libs.cardstackview)
    implementation(libs.cardview)
    implementation(libs.swiperefreshlayout)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)

    // Navigation
    implementation(libs.navigation.fragment.v277)
    implementation(libs.navigation.ui)

    // Image Loading & Network
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.gson)

    // Location Services
    implementation(libs.play.services.location)
    implementation(libs.annotation)
}