# ============================================================
# iChatAI - R8
# ============================================================

# Generic types / reflection
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Runtime annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile


# ============================================================
# Gson
# ============================================================

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}


# ============================================================
# Retrofit
# ============================================================

-keepattributes Exceptions

# Retrofit service interfaces
-keep,allowoptimization,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Kotlin suspend functions / Retrofit generic signatures
-keep class kotlin.coroutines.Continuation


# ============================================================
# iChatAI Gson models
# ============================================================

# Temporary targeted protection while stabilizing release build.
# Once everything works, we can reduce this further.

-keep class com.zafar.ichatai.data.ChatBotDataKt { *; }

-keep class com.zafar.ichatai.data.** { *; }
-keep class com.zafar.ichatai.model.** { *; }


# ============================================================
# Parcelable
# ============================================================

-keepclassmembers class * implements android.os.Parcelable {
    public static ** CREATOR;
}