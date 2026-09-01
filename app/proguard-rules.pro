# ============================================================
# iChatAI - R8 / ProGuard Rules
# ============================================================

# Generic types / reflection / annotations
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# Project Specific Protection
# ============================================================

# Keep all project classes to avoid any stripping of critical logic
-keep class com.zafar.ichatai.network.** { *; }
-keep class com.zafar.ichatai.data.** { *; }
-keep class com.zafar.ichatai.model.** { *; }
-keep class com.zafar.ichatai.utils.** { *; }
-keep class com.zafar.ichatai.viewmodel.** { *; }
-keep class com.zafar.ichatai.ui.** { *; }

# Preserve BuildConfig as it contains GitHub tokens and repo info
-keep class com.zafar.ichatai.BuildConfig { *; }

# ============================================================
# Retrofit 2
# ============================================================

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations

-keep interface retrofit2.** { *; }

# Retrofit service interfaces
-keep interface * {
    @retrofit2.http.* <methods>;
}

# Kotlin suspend functions / Retrofit generic signatures
-keep class kotlin.coroutines.Continuation
-keep class kotlin.coroutines.jvm.internal.** { *; }

# ============================================================
# OkHttp 3
# ============================================================

-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# ============================================================
# Gson
# ============================================================

-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn com.google.gson.**

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================
# Google API Client & Drive
# ============================================================

-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.json.gson.** { *; }
-keep interface com.google.api.client.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.drive.**

# Google API client reflection rules
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations

# Needed for NetHttpTransport
-keep class com.google.api.client.http.javanet.** { *; }
-keep class com.google.api.client.http.apache.** { *; }

# Prevent stripping of Google services model classes
-keep class com.google.api.services.drive.model.** { *; }

# ============================================================
# Hilt / Dagger
# ============================================================

-keep class dagger.hilt.** { *; }
-keep class com.zafar.ichatai.di.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider

# ============================================================
# Android Components & Coroutines
# ============================================================

-keepclassmembers class * implements android.os.Parcelable {
    public static ** CREATOR;
}

-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
