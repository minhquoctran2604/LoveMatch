plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.ksp) apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}