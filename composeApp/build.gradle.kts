import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.google.gms.google-services")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            
            // Firebase AI dependencies
            implementation("com.google.firebase:firebase-ai:17.0.0")
            implementation("com.google.firebase:firebase-auth:23.0.0")
            implementation("com.google.firebase:firebase-bom:33.7.0")
            
            // Markwon for markdown rendering
            implementation("io.noties.markwon:core:4.6.2")
            implementation("io.noties.markwon:html:4.6.2")
            implementation("io.noties.markwon:image:4.6.2")
            implementation("io.noties.markwon:linkify:4.6.2")
            implementation("io.noties.markwon:ext-tables:4.6.2")
            implementation("io.noties.markwon:ext-strikethrough:4.6.2")
            implementation("io.noties.markwon:ext-tasklist:4.6.2")
            
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
            
            // Lifecycle
            implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
            
            // Room database (if needed for data persistence)
            implementation("androidx.room:room-runtime:2.6.1")
            implementation("androidx.room:room-ktx:2.6.1")
            
            // File provider
            implementation("androidx.core:core-ktx:1.17.0")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.ratnesh.callrecordsummary"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ratnesh.callrecordsummary"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

