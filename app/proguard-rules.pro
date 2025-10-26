# Navigation Component
   #-keepnames class androidx.navigation.fragment.NavHostFragment
   #-keep class * extends androidx.navigation.Navigator

   # Compose
   #-keep class androidx.compose.** { *; }
   #-keepclassmembers class androidx.compose.** { *; }

   # Flotilla Navigation
   #-keep class com.imdoctor.flotilla.presentation.navigation.** { *; }
   #-keepclassmembers class com.imdoctor.flotilla.presentation.navigation.** { *; }