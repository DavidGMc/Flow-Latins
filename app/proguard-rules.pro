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
-dontobfuscate
-dontwarn lombok.NonNull
# Mantener intactos los paquetes específicos
-keep class cd.software.flowirc.admob.** { *; }
-keep class cd.software.flowirc.firebase.** { *; }
-keep class cd.software.flowirc.model.** { *; }

# Reglas generales para Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Para Firebase general
-keep class com.google.firebase.** { *; }
-keep class com.firebase.** { *; }

# Para AdMob
-keep class com.google.android.gms.ads.** { *; }
# Mantener metadatos de Kotlin para serialización
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class cd.software.flowchat.firebase.model.**$$serializer { *; }
-keepclassmembers class cd.software.flowchat.firebase.model.** {
    *** Companion;
}

# Conserva la clase completa y todos sus miembros públicos
-keep class cd.software.flowchat.firebase.model.UserProfile {
    public <init>();
    public *;
}


# Específicamente para UserProfile
-keep class cd.software.flowchat.firebase.model.UserProfile
-keepclassmembers class cd.software.flowchat.firebase.model.UserProfile { *; }

# Regla para data classes de Kotlin
-keepclassmembers class * {
    ** component*();
    ** copy*(...);
}

# Mantener paquetes completos
-keep class cd.software.flowchat.firebase.model.** { *; }