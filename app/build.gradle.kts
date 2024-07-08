import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.assert)
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("/var/home/renattele/AndroidStudioProjects/Uniboard/test_key.jks")
            storePassword = "12345678"
            keyAlias = "key0"
            keyPassword = "12345678"
        }
    }
    namespace = "com.uniboard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.uniboard"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("boolean", "SHOW_BORDERS", rootProject.properties["app.show_borders"]?.toString() ?: "false")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            pickFirsts += "META-INF/*"
        }
    }
    buildFeatures{
        viewBinding = true
    }
    viewBinding {
        val enabled = true
    }
}

// Not working yet
@OptIn(ExperimentalKotlinGradlePluginApi::class)
powerAssert {
    functions = listOf("kotlin.assert", "kotlin.assertEquals")
}

dependencies {
    implementation(libs.material.v140)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.icons)
    implementation(libs.kotlin.serialization)

    implementation(platform(libs.http4k.bom))
    implementation(libs.http4k.core)
    implementation(libs.http4k.client)
    implementation(libs.http4k.serialization)
    implementation(libs.http4k.multipart)

    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.molecule)
    implementation(libs.coil)

    implementation(libs.socketio)


    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}