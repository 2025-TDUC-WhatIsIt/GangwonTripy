# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes *Annotation*
-keep class com.whatisit.gangwontripy.data.model.** { *; }

# Kakao Vector Map v2
-keep class com.kakao.vectormap.** { *; }
-keep class com.kakao.vectortile.** { *; }
-keep class com.kakao.sdk.common.** { *; }
-dontwarn com.kakao.**

# ===================================================================
# GSON 라이브러리에서 사용하는 데이터 모델 클래스들은 절대 건드리지 않도록 설정
# (클래스 이름, 필드, 메소드 모두 유지)
# ===================================================================
# 아래에 GSON으로 파싱하는 모든 모델 클래스의 전체 경로를 추가해주세요.

-keep class com.whatisit.gangwontripy.ui.directions.DirectionsFragment$Poi { *; }
-keep class com.whatisit.gangwontripy.data.model.VisitItem { *; }
-keep class com.whatisit.gangwontripy.data.model.YearItem { *; }
-keep class com.whatisit.gangwontripy.data.model.TouristSpotItem { *; }
# ... (다른 모델 클래스가 있다면 계속 추가)