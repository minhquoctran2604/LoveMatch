plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
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

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"AIzaSyAdJv3gEKJxHW76wTER4mVPh1gTUHszmhM\""
        )
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
            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"AIzaSyAdJv3gEKJxHW76wTER4mVPh1gTUHszmhM\""
            )
        }
        debug {
            buildConfigField(
                "String",
                "GEMINI_API_KEY",
                "\"AIzaSyAdJv3gEKJxHW76wTER4mVPh1gTUHszmhM\""
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.cardview)
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Firebase (bom & services)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)

    // 3rd-party libraries
    implementation(libs.cardstackview)
    implementation(libs.glide)
    implementation(libs.annotation)
    implementation(libs.play.services.location)
    implementation(libs.okhttp)
    implementation(libs.gson)

    // RxJava 3
    implementation(libs.rxjava3)
    implementation(libs.rxandroid3)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Unit Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
