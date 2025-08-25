// Top-level build.gradle.kts

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}


// Define your Java version directly
val projectJavaVersion = JavaVersion.VERSION_11

// If you need to access this in subprojects, you can configure it there directly
// or use 'subprojects' or 'allprojects' blocks if needed, though often it's
// clearer to set it in each module's build.gradle.kts where compileOptions are defined.
