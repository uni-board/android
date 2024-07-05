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
-dontwarn org.brotli.dec.BrotliInputStream
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontobfuscate
-keep class org.json.** {
    *;
}

-keep class org.http4k.** {
    *;
}
-keep class org.apache.** {
    *;
}
-dontwarn com.sun.net.httpserver.Headers
-dontwarn com.sun.net.httpserver.HttpContext
-dontwarn com.sun.net.httpserver.HttpExchange
-dontwarn com.sun.net.httpserver.HttpHandler
-dontwarn com.sun.net.httpserver.HttpServer
-dontwarn dev.forkhandles.result4k.Failure
-dontwarn dev.forkhandles.result4k.Result
-dontwarn dev.forkhandles.result4k.Success
-dontwarn dev.forkhandles.values.OrNullKt
-dontwarn dev.forkhandles.values.Value
-dontwarn dev.forkhandles.values.ValueFactory
-dontwarn jakarta.servlet.ServletInputStream
-dontwarn jakarta.servlet.ServletOutputStream
-dontwarn jakarta.servlet.http.HttpServlet
-dontwarn jakarta.servlet.http.HttpServletRequest
-dontwarn jakarta.servlet.http.HttpServletResponse
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn java.net.http.HttpClient$Builder
-dontwarn java.net.http.HttpClient$Redirect
-dontwarn java.net.http.HttpClient$Version
-dontwarn java.net.http.HttpClient
-dontwarn java.net.http.HttpHeaders
-dontwarn java.net.http.HttpRequest$BodyPublisher
-dontwarn java.net.http.HttpRequest$BodyPublishers
-dontwarn java.net.http.HttpRequest$Builder
-dontwarn java.net.http.HttpRequest
-dontwarn java.net.http.HttpResponse$BodyHandler
-dontwarn java.net.http.HttpResponse$BodyHandlers
-dontwarn java.net.http.HttpResponse
-dontwarn java.net.http.HttpTimeoutException
-dontwarn javax.servlet.ServletInputStream
-dontwarn javax.servlet.ServletOutputStream
-dontwarn javax.servlet.http.HttpServlet
-dontwarn javax.servlet.http.HttpServletRequest
-dontwarn javax.servlet.http.HttpServletResponse
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid