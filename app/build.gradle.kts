plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("kotlin-kapt") // Sử dụng id thay cho alias để tránh xung đột
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

        // Định nghĩa GEMINI_API_KEY
        buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyAdJv3gEKJxHW76wTER4mVPh1gTUHszmhM\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true // Thêm dòng này để kích hoạt BuildConfig
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyAdJv3gEKJxHW76wTER4mVPh1gTUHszmhM\"")
        }
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyAdJv3gEKJxHW76wTER4mVPh1gTUHszmhM\"")
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
    implementation(libs.navigation.fragment)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Thêm thư viện CardStackView để hỗ trợ tính năng vuốt
    implementation(libs.cardstackview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.navigation.fragment.v277)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.storage)
    implementation(libs.material)
    implementation(libs.glide)
    implementation(libs.annotation)
    implementation(libs.play.services.location)
    implementation(libs.cardview)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.firebase.messaging)

    // RxJava3 components
    implementation(libs.rxjava3)
    implementation(libs.rxandroid3)

    // Room components
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

}
