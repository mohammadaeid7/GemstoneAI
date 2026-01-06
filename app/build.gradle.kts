plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.gemstoneai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gemstoneai"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug build
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // Compose compiler extension version per AndroidX Compose release docs.
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM (يختار نسخ Compose المتوافقة تلقائياً)
    implementation(platform("androidx.compose:compose-bom:+"))

    // AndroidX الأساسيات
    implementation("androidx.core:core-ktx:+")
    implementation("androidx.activity:activity-compose:+")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:+")
    implementation("androidx.navigation:navigation-compose:+")
    implementation("androidx.datastore:datastore-preferences:+")

    // Compose (بدون أرقام إصدارات)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ML Kit (اتركه ثابتاً)
    implementation("com.google.mlkit:image-labeling:17.0.9")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:+")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
