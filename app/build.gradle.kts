import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gangwontripy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gangwontripy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        val props = Properties()
        props.load(rootProject.file("local.properties").inputStream())

        val kakaoApiKey = props.getProperty("KAKAO_API_KEY")
            ?: throw GradleException("KAKAO_API_KEY not found in local.properties")

        manifestPlaceholders["kakao_api_key"] = kakaoApiKey
        buildConfigField("String", "KAKAO_MAP_KEY", "\"$kakaoApiKey\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
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
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.maps)

    // Jetpack Navigation Component
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    implementation("com.kakao.maps.open:android:2.12.8")
    implementation("com.kakao.sdk:v2-common:2.20.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}