# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Android\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# Add this global rule
-keepattributes Signature

-keepattributes *Annotation*

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.
-keepclassmembers class com.rndtechnosoft.fynder.model.** {
  *;
}

-keep class com.rndtechnosoft.fynder.adapter.* { *; }

-keep class com.rndtechnosoft.fynder.dialog.* { *; }


##new chat proguard rules for firebase

# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models. Modify to fit the structure
# of your app.


-keep class com.rndtechnosoft.fynder.views.* { *; }

#okio
-dontwarn okio.**

-dontwarn org.apache.regexp.**
-dontwarn org.apache.commons.codec.binary.**
-dontwarn java.lang.management.**
-keep class org.apache.regexp.**
-keep class org.apache.commons.codec.binary.**
-keep class java.lang.management.**
-keep class org.apache.lucene.codecs.Codec
-keep class * extends org.apache.lucene.codecs.Codec
-keep class org.apache.lucene.codecs.PostingsFormat
-keep class * extends org.apache.lucene.codecs.PostingsFormat
-keep class org.apache.lucene.codecs.DocValuesFormat
-keep class * extends org.apache.lucene.codecs.DocValuesFormat
-keep class org.apache.lucene.analysis.tokenattributes.**
-keep class org.apache.lucene.**Attribute
-keep class * implements org.apache.lucene.**Attribute


#android support lib
-keep class android.support.** { *; }

# String Similarity
-keep class info.debatty.java.stringsimilarity.**

-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}



#Fresco
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**



-assumenosideeffects class android.util.Log {
  public static *** d(...);
  public static *** w(...);
  public static *** v(...);
  public static *** e(...);
  public static *** i(...);
}

## New rules for EventBus 3.0.x ##
# http://greenrobot.org/eventbus/documentation/proguard/

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#ucrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

