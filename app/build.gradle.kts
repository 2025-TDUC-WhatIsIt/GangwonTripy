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
    namespace = "com.example.gangwontripy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gangwontripy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 🔐 키 로딩
        val tourApiKey  = secret("TOUR_API_SERVICE_KEY")
        val kakaoApiKey = secret("KAKAO_API_KEY") // 네가 이미 쓰고 있는 이름 유지

        // 매니페스트 치환(카카오 SDK 공통/로그인 등에서 사용)
        manifestPlaceholders["kakao_api_key"] = kakaoApiKey

        // 코드에서 사용
        buildConfigField("String", "KAKAO_MAP_KEY", "\"$kakaoApiKey\"")
        buildConfigField("String", "TOUR_API_SERVICE_KEY", "\"$tourApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { buildConfig = true }

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
    // 버전 카탈로그(libs.*)만 사용하도록 정리
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.play.services.maps)

    implementation("com.kakao.maps.open:android:2.12.8")
    implementation("com.kakao.sdk:v2-common:2.20.1")

    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
