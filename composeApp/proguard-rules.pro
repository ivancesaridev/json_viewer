# R8 rules for JSON Viewer.
#
# The app enables minification and resource shrinking for release builds. Everything reachable only
# through reflection has to be kept explicitly — in this project that is kotlinx.serialization
# (used for the recent-files store and for type-safe navigation routes).

# ---------------------------------------------------------------------------
# kotlinx.serialization
# ---------------------------------------------------------------------------
# Keep the generated `Companion.serializer()` entry points and the synthetic `$serializer` classes
# that back every @Serializable type. Without these, R8 strips the serializers and the app throws
# SerializationException at runtime instead of failing at build time.

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# The app's own serializable models and navigation routes.
-keep @kotlinx.serialization.Serializable class org.ivancesari.jsonreader.** { *; }
-keep class org.ivancesari.jsonreader.**$$serializer { *; }

# ---------------------------------------------------------------------------
# Navigation Compose (type-safe routes are resolved reflectively)
# ---------------------------------------------------------------------------
-keep class androidx.navigation.** { *; }
-keepclassmembers class androidx.navigation.serialization.** { *; }

# ---------------------------------------------------------------------------
# Coroutines
# ---------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---------------------------------------------------------------------------
# DataStore / Okio
# ---------------------------------------------------------------------------
-dontwarn okio.**
-dontwarn org.jetbrains.annotations.**

# ---------------------------------------------------------------------------
# Keep line numbers so Play Console crash reports stay readable after deobfuscation.
# ---------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
