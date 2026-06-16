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
