plugins {
    alias(libs.plugins.android.application)
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.cardview)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Glide & Networking
    implementation(libs.glide)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.play.services.location)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext)
    androidTestImplementation(libs.espresso.core)

    // CardStackView từ Jitpack
    implementation("com.github.yuyakaido:CardStackView:2.3.4")
}
