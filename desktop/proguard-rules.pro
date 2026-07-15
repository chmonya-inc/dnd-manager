# Keep the Java HTTP client and its inner structures
-keep class java.net.http.** { *; }
-dontwarn java.net.http.**

# Common library warnings
-dontwarn org.slf4j.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.poi.**
-dontwarn kotlinx.coroutines.**
-dontwarn io.ktor.**
-dontwarn kotlin.reflect.**
-dontwarn okio.**
-dontwarn javax.naming.**
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn org.apache.logging.log4j.**
-dontwarn org.osgi.**
-dontwarn aQute.bnd.annotation.spi.**
-dontwarn org.apache.batik.**

# Ignore all unresolved references to proceed with the build
# Since we have 11k+ warnings, this is the most practical way to unblock the build.
-ignorewarnings

# Keep common entry points
-keepclasseswithmembers class * {
    public static void main(java.lang.String[]);
}

# --- ServiceLoader providers ---
# These classes are listed in META-INF/services/<interface> and instantiated
# reflectively by java.util.ServiceLoader. ProGuard sees no static references,
# so it strips them unless we keep them. One rule per service interface in the
# uber jar (interface FQN == the service filename). { *; } keeps name + members.
-keep class * implements coil3.util.FetcherServiceLoaderTarget { *; }
-keep class * implements io.ktor.client.HttpClientEngineContainer { *; }
-keep class * implements io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }
-keep class * implements kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keep class * implements org.apache.logging.log4j.util.PropertySource { *; }
-keep class * implements org.apache.poi.extractor.ExtractorProvider { *; }
-keep class * implements org.apache.poi.sl.draw.ImageRenderer { *; }
-keep class * implements org.apache.poi.sl.usermodel.MetroShapeProvider { *; }
-keep class * implements org.apache.poi.sl.usermodel.SlideShowProvider { *; }
-keep class * implements org.apache.poi.ss.usermodel.WorkbookProvider { *; }

# --- kotlinx.serialization ---
# @Serializable classes get a compiler-generated `$$serializer` accessed
# reflectively via the Companion.serializer() function; ProGuard strips both
# unless kept. Covers app models/DTOs (com.dnd.helper.**) and library types.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
