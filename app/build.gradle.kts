import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val geminiApiKey: String = (localProperties.getProperty("apiKey")
    ?: localProperties.getProperty("GEMINI_API_KEY")
    ?: localProperties.getProperty("gemini.api.key")
    ?: (project.findProperty("GEMINI_API_KEY") as? String)
    ?: (project.findProperty("apiKey") as? String)
    ?: System.getenv("GEMINI_API_KEY")
    ?: System.getenv("apiKey")
    ?: "")

android {
    namespace = "com.sanjana.mornote"

    //noinspection GradleDependency
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sanjana.mornote"
        minSdk = 24

        //noinspection OldTargetApi
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
        buildConfigField("String", "API_KEY", "\"$geminiApiKey\"")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }
    //noinspection composeOptions
    @Suppress("UnstableApiUsage")
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Room components
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler) // KSP handles the code generation
    // Kotlin Coroutines for Room
    implementation(libs.androidx.room.ktx)

    // Biometric authentication
    implementation(libs.androidx.biometric)

    // Security & EncryptedSharedPreferences
    implementation(libs.androidx.security.crypto)
    implementation("com.scottyab:rootbeer-lib:0.1.0")

// Lifecycle components for ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

// Navigation for Compose
    implementation(libs.androidx.navigation.compose)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)

// Existing libs.androidx dependencies...
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    // Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation(libs.firebase.analytics)

    // Add the dependencies for any other desired Firebase products
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.functions.ktx)

    // Necessary for .await() functionality
    implementation(libs.kotlinx.coroutines.play.services)

    // Credential Manager (2025 Standard)
    //noinspection GradleDependency
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.coil.compose)
    implementation(libs.firebase.config.ktx)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}
