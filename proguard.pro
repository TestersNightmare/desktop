# The rules from AOSP are located in proguard.flags file, we can just maintain Desktop related rules here.

# Optimization options.
-allowaccessmodification
-dontoptimize
-dontpreverify
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-keepattributes InnerClasses, *Annotation*, Signature, SourceFile, LineNumberTable


# This is generated automatically by the Android Gradle plugin.
-dontwarn android.appwidget.AppWidgetHost$AppWidgetHostListener
-dontwarn android.util.StatsEvent$Builder
-dontwarn android.util.StatsEvent
-dontwarn androidx.window.extensions.**
-dontwarn androidx.window.sidecar.**
-dontwarn com.android.org.conscrypt.TrustManagerImpl
-dontwarn com.android.wm.shell.**
-dontwarn com.skydoves.balloon.**
-dontwarn dalvik.system.CloseGuard
-dontwarn lineageos.providers.LineageSettings$System
-dontwarn androidx.compose.runtime.PrimitiveSnapshotStateKt
-dontwarn androidx.renderscript.Allocation
-dontwarn androidx.renderscript.BaseObj
-dontwarn androidx.renderscript.Element
-dontwarn androidx.renderscript.FieldPacker
-dontwarn androidx.renderscript.RSRuntimeException
-dontwarn androidx.renderscript.RenderScript
-dontwarn androidx.renderscript.Script$LaunchOptions
-dontwarn androidx.renderscript.ScriptC
-dontwarn androidx.renderscript.ScriptIntrinsicBlur
-dontwarn androidx.renderscript.Type


# Common rules.
-keep class com.android.** { *; }
-keep class android.window.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements android.os.Parcelable {
  public static final ** CREATOR;
}

# Desktop specific rules.
-keep class app.desktop.DesktopProto$* { *; }
-keep class app.desktop.DesktopApp { *; }
-keep class app.desktop.DesktopLauncher { *; }
-keep class app.desktop.compatlib.** { *; }
-keep class android.view.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

-keep class com.google.protobuf.Timestamp { *; }
# 保留 Firebase 核心功能
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
# 保留 Analytics 所需类
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.android.gms.measurement.**

# TODO: Remove this after the change in https://github.com/ChickenHook/RestrictionBypass/pull/9 has been released.
-keep class org.chickenhook.restrictionbypass.** { *; }

# Vungle
-dontwarn com.vungle.ads.**
-keepclassmembers class com.vungle.ads.** {
  *;
}
-keep class com.vungle.ads.**



# Google
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**




# START OkHttp + Okio
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**


# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# END OkHttp + Okio


# START Protobuf
-dontwarn com.google.protobuf.**
-keepclassmembers class com.google.protobuf.** {
 *;
}
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }


# END Protobuf
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }
-keep public class com.mbridge.* extends androidx.** { *; }
-keep public class androidx.viewpager.widget.PagerAdapter{ *; }
-keep public class androidx.viewpager.widget.ViewPager.OnPageChangeListener{ *; }
-keep interface androidx.annotation.IntDef{ *; }
-keep interface androidx.annotation.Nullable{ *; }
-keep interface androidx.annotation.CheckResult{ *; }
-keep interface androidx.annotation.NonNull{ *; }
-keep public class androidx.fragment.app.Fragment{ *; }
-keep public class androidx.core.content.FileProvider{ *; }
-keep public class androidx.core.app.NotificationCompat{ *; }
-keep public class androidx.appcompat.widget.AppCompatImageView { *; }
-keep public class androidx.recyclerview.*{ *; }

-keep class jp.maio.sdk.android.** {
      *;
}
-keep class com.startapp.** {
      *;
}

-keep class com.truenet.** {
      *;
}

-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile,
LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn android.webkit.JavascriptInterface
-dontwarn com.startapp.**

-dontwarn org.jetbrains.annotations.**
# Vungle
-dontwarn com.vungle.ads.**
-keepclassmembers class com.vungle.ads.** {
  *;
}
-keep class com.vungle.ads.**



# Google
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**




# START OkHttp + Okio
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**


# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# END OkHttp + Okio


# START Protobuf
-dontwarn com.google.protobuf.**
-keepclassmembers class com.google.protobuf.** {
 *;
}
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }


# END Protobuf
-keep class jp.maio.sdk.android.** {
      *;
}
-keep class com.startapp.** {
      *;
}

-keep class com.truenet.** {
      *;
}

-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile,
LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn android.webkit.JavascriptInterface
-dontwarn com.startapp.**

-dontwarn org.jetbrains.annotations.**
# Vungle
-dontwarn com.vungle.ads.**
-keepclassmembers class com.vungle.ads.** {
  *;
}
-keep class com.vungle.ads.**



# Google
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**




# START OkHttp + Okio
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**


# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**


# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*


# END OkHttp + Okio


# START Protobuf
-dontwarn com.google.protobuf.**
-keepclassmembers class com.google.protobuf.** {
 *;
}
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }


# END Protobuf
-keepclassmembers class com.ironsource.sdk.controller.IronSourceWebView$JSInterface {
    public *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep class com.ironsource.adapters.** { *;
}
-dontwarn com.ironsource.mediationsdk.**
-dontwarn com.ironsource.adapters.**
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes Signature
-keep class net.pubnative.** { *; }
-keep class com.iab.omid.library.pubnativenet.** { *; }
-keep class com.zeus.gmc.sdk.mobileads.**{*;}
-keep public class com.smaato.sdk.** { *; }
-keep public interface com.smaato.sdk.** { *; }
-keep class com.bytedance.sdk.** { *; }
-keep class com.amazon.** { *; }
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep class com.iabtcf.** {*;}
-keep class com.kidoz.** { *;}
-keepclassmembers class com.kidoz.** {
   *;
}

-keep class com.squareup.imagelib.** { *;}
-keepclassmembers class com.squareup.imagelib.** {
   *;
}
-keep class com.huawei.openalliance.ad.** { *; }
-keep class com.huawei.hms.ads.** { *; }
-keep class com.inmobi.** { *; }
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
     public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
# skip IAB classes
-keep class com.iab.** {*;}
-dontwarn com.iab.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-dontwarn com.mbridge.**
-keepclassmembers class **.R$* { public static final int mbridge*; }

-keep public class com.mbridge.* extends androidx.** { *; }
-keep public class androidx.viewpager.widget.PagerAdapter{*;}
-keep public class androidx.viewpager.widget.ViewPager.OnPageChangeListener{*;}
-keep interface androidx.annotation.IntDef{*;}
-keep interface androidx.annotation.Nullable{*;}
-keep interface androidx.annotation.CheckResult{*;}
-keep interface androidx.annotation.NonNull{*;}
-keep public class androidx.fragment.app.Fragment{*;}
-keep public class androidx.core.content.FileProvider{*;}
-keep public class androidx.core.app.NotificationCompat{*;}
-keep public class androidx.appcompat.widget.AppCompatImageView {*;}
-keep public class androidx.recyclerview.*{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV$Builder{*;}
-keep public class io.bidmachine.** { *; }
-keep public interface io.bidmachine.** { *; }
-keep class com.chartboost.** { *; }
-keep class com.my.target.** {*;}
