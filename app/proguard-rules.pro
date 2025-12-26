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

# ============================================
# ViewModels (критично для ViewModelProvider.Factory)
# ============================================
# Сохранить все ViewModels и их конструкторы
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ViewModelFactory должен сохранить методы create
-keep class com.imdoctor.flotilla.presentation.ViewModelFactory { *; }
-keepclassmembers class com.imdoctor.flotilla.presentation.ViewModelFactory { *; }

# ============================================
# Kotlin Objects (критично для LocaleManager)
# ============================================
# Сохранить INSTANCE поле для всех Kotlin objects
-keepclassmembers class * {
    public static ** INSTANCE;
}

# Сохранить LocaleManager полностью
-keep class com.imdoctor.flotilla.utils.LocaleManager { *; }
-keepclassmembers class com.imdoctor.flotilla.utils.LocaleManager { *; }

# Сохранить все утилиты
-keep class com.imdoctor.flotilla.utils.** { *; }

# ============================================
# DataStore Preferences (критично для настроек)
# ============================================
# Сохранить SettingsSnapshot и все data классы в preferences
-keep class com.imdoctor.flotilla.data.local.preferences.** { *; }
-keepclassmembers class com.imdoctor.flotilla.data.local.preferences.** { *; }

# ============================================
# Compose UI Components
# ============================================
# Сохранить все компоненты (включая LanguageSwitcher)
-keep class com.imdoctor.flotilla.presentation.components.** { *; }
-keepclassmembers class com.imdoctor.flotilla.presentation.components.** { *; }

# ============================================
# AI Components
# ============================================
# Сохранить AI интерфейсы и реализации
-keep interface com.imdoctor.flotilla.presentation.screens.game.ai.AIOpponent { *; }
-keep class com.imdoctor.flotilla.presentation.screens.game.ai.** { *; }
-keepclassmembers class com.imdoctor.flotilla.presentation.screens.game.ai.** { *; }

# ============================================
# AndroidX AppCompat / Localization (критично для LocaleManager)
# ============================================
# Сохранить AppCompatDelegate для setApplicationLocales()
-keep class androidx.appcompat.app.AppCompatDelegate { *; }
-keepclassmembers class androidx.appcompat.app.AppCompatDelegate { *; }

# Сохранить LocaleListCompat
-keep class androidx.core.os.LocaleListCompat { *; }
-keepclassmembers class androidx.core.os.LocaleListCompat { *; }

# Сохранить BuildCompat для проверки версий API
-keep class androidx.core.os.BuildCompat { *; }
