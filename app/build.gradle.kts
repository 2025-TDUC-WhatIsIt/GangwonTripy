import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use(::load)
}
fun secret(name: String): String =
    (providers.gradleProperty(name).orNull
        ?: providers.environmentVariable(name).orNull
        ?: localProps.getProperty(name))
        ?: throw GradleException("Missing secret: $name (put it in local.properties or env/gradle.properties)")

android {
    namespace = "com.whatisit.gangwontripy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.whatisit.gangwontripy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 🔐 키 로딩
        val tourApiKey  = secret("TOUR_API_SERVICE_KEY")
        val kakaoApiKey = secret("KAKAO_API_KEY")
        val yourIp = secret("YOUR_IP")
        // 매니페스트 치환(카카오 SDK 공통/로그인 등에서 사용)
        manifestPlaceholders["kakao_api_key"] = kakaoApiKey

        // 코드에서 사용
        buildConfigField("String", "KAKAO_MAP_KEY", "\"$kakaoApiKey\"")
        buildConfigField("String", "TOUR_API_SERVICE_KEY", "\"$tourApiKey\"")
        buildConfigField ("String", "API_BASE", "\"$yourIp\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { buildConfig = true }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
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

    implementation("com.kakao.maps.open:android:2.12.8")
    implementation("com.kakao.sdk:v2-common:2.20.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.1.0")
    implementation ("androidx.camera:camera-core:1.3.4")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-view:1.3.4")
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
