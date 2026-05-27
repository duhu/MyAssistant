# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes Signature

# Retrofit
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.myassistant.app.**$$serializer { *; }
-keepclassmembers class com.myassistant.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.myassistant.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
