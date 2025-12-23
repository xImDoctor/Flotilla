# ============================================
# Flotilla ProGuard Rules
# ============================================
# These rules prevent code shrinking from breaking critical functionality
# in Release builds (minifyEnabled = true)

# ============================================
# DataStore (критично для сохранения настроек)
# ============================================
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-keep class androidx.datastore.*.** { *; }

# ============================================
# Firebase Authentication
# ============================================
-keep class com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** { *; }

# ============================================
# Firebase Firestore
# ============================================
-keep class com.google.firebase.firestore.** { *; }
-keepclassmembers class com.google.firebase.firestore.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Firestore модели (с @PropertyName, @ServerTimestamp)
-keep class com.imdoctor.flotilla.data.remote.firebase.models.** { *; }
-keepclassmembers class com.imdoctor.flotilla.data.remote.firebase.models.** { *; }

# ============================================
# Kotlinx Serialization (критично для WebSocket)
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.imdoctor.flotilla.**$$serializer { *; }
-keepclassmembers class com.imdoctor.flotilla.** {
    *** Companion;
}
-keepclasseswithmembers class com.imdoctor.flotilla.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# WebSocket модели с @Serializable
-keep class com.imdoctor.flotilla.data.remote.websocket.models.** { *; }
-keepclassmembers class com.imdoctor.flotilla.data.remote.websocket.models.** { *; }

# ============================================
# OkHttp / WebSocket (критично для онлайн игр)
# ============================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ============================================
# Gson (используется в зависимостях)
# ============================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ============================================
# Сохранить все data классы приложения
# ============================================
-keep class com.imdoctor.flotilla.data.** { *; }
-keepclassmembers class com.imdoctor.flotilla.data.** { *; }

# ============================================
# Сохранить enum классы
# ============================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================
# Navigation Component
# ============================================
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.navigation.Navigator

# ============================================
# Compose
# ============================================
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Flotilla Navigation
-keep class com.imdoctor.flotilla.presentation.navigation.** { *; }
-keepclassmembers class com.imdoctor.flotilla.presentation.navigation.** { *; }
