import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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

        // Read GEMINI_API_KEY from local.properties
        val geminiApiKey = try {
            val properties = Properties()
            val localPropertiesFile = project.rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                FileInputStream(localPropertiesFile).use { input ->
                    properties.load(input)
                }
                properties.getProperty("GEMINI_API_KEY") ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Disabled for debugging
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val geminiApiKey = try {
                val properties = Properties()
                val localPropertiesFile = project.rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    FileInputStream(localPropertiesFile).use { input ->
                        properties.load(input)
                    }
                    properties.getProperty("GEMINI_API_KEY") ?: ""
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        }
        debug {
            val geminiApiKey = try {
                val properties = Properties()
                val localPropertiesFile = project.rootProject.file("local.properties")
                if (localPropertiesFile.exists()) {
                    FileInputStream(localPropertiesFile).use { input ->
                        properties.load(input)
                    }
                    properties.getProperty("GEMINI_API_KEY") ?: ""
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CardStackView
    implementation(libs.cardstackview)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.app.check) // Fixed App Check dependency
    implementation(libs.glide)
    implementation(libs.annotation)
    implementation(libs.play.services.location)
    implementation(libs.cardview)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.lottie)

    // RxJava3
    implementation(libs.rxjava3)
    implementation(libs.rxandroid3)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // Nếu bạn dùng annotation processor cho Room, hãy thêm dòng sau:
    // kapt(libs.room.compiler)
    // Nếu bạn dùng KSP cho Room, hãy thêm dòng sau:
    // ksp(libs.room.compiler)
}

