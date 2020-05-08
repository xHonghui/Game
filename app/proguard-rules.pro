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
# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
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

#NOTE!!!
#Before you add any specific keep options, see the file ${sdk.dir}/tools/proguard/proguard-android.txt what have be specify, and don't specify them again.

# here we go !!!!

#Producing useful obfuscated stack traces
-printmapping out.map

#-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# the SDK stuffs
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepattributes Exceptions,InnerClasses


-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.database.sqlite.SQLiteOpenHelper
-keep class com.android.vending.billing.**

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class org.apache.** {*;}
-dontwarn java.nio.file.**

-keepattributes InnerClasses, EnclosingMethod

# the jiubang commerce  ads APIs
-dontwarn com.loopme.**
-keep class com.loopme.** { *; }
-keep interface com.loopme.** { *; }
-keep public class com.cs.bd.database.DataBaseHelper{*;}
-keep class com.jb.ga0.commerce.util.**{*;}
-dontwarn com.cs.bd.commerce.util.**
-dontwarn okhttp3.**

# the event bus APIs
-dontwarn  de.greenrobot.event.util.**
-keepclassmembers  class * {
   public void onEvent*(***);
}

-dontnote com.cs.utils.net.request.THttpRequest
-dontnote com.cs.utils.net.response.IResponse

# the aidl package
-keep class android.content.pm.** { *; }
-keep interface android.content.pm.** { *; }

# the statics APIs
-dontwarn com.cs.statistic.**
-keep class com.cs.statistic.R { *; }
-keep class com.cs.statistic.R$* { *; }


#用户反馈代码不混淆
-keep class com.jiubang.core.util.** { *; }
#Android标注代码不混淆
-keep class android.annotation.** { *; }
#support v4 代码不混淆
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

#创建DB类不进行代码混淆
-keep public class com.gto.store.core.database.DataBaseHelper { *; }
#-keep class android.content.pm.** { *; }

#adsdk混淆配置===BEGIN=========
-dontwarn android.webkit.*
#DB创建类(使用到反射)
-keep public class com.cs.bd.ad.sdk.SdkAdSourceListener{*;}
-keep public class com.cs.bd.ad.AdSdkApi{*;}
-keep public class com.cs.bd.ad.bean.** {*;}
-keep public class com.cs.bd.ad.sdk.** {*;}
-keep public interface com.cs.bd.ad.manager.**{*;}
-keep public class com.cs.bd.utils.StringUtils{*;}
-keep public class com.cs.bd.utils.AdTimer{*;}
-keep public class com.cs.bd.ad.params.**{*;}
-keep public class com.cs.bd.ad.http.bean.BaseModuleDataItemBean{*;}
-keep public class com.cs.bd.ad.http.AdSdkRequestHeader$S2SParams{*;}
-keep class com.cs.bd.chargelocker.statistic.plugin.PluginStatistic{*;}
-keep public class com.cs.bd.chargelocker.statistic.**{*;}
-keep public interface com.cs.bd.dynamicloadlib.framework.inter.IPluginParamsProxy{*;}
-keep public interface com.cs.bd.dynamicloadlib.framework.inter.IFrameworkCenterCallBack{*;}
-keep class com.cs.bd.dynamicloadlib.**{*;}
-keep public class com.cs.bd.ad.cache.config.**{*;}
#adsdk混淆配置===END===========


# 如果使用了tbs版本的sdk需要进行以下配置
-keep class com.tencent.smtt.** { *; }
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
#GDTSdk=====END============
-keep class com.appsflyer.** { *; }
-keep class com.bun.miitmdid.core.** {*;} #国内oaid
-keep class com.bun.miitmdid.** { *; }
-keep class com.huawei.hms.pps.** { *; }
-keep interface com.huawei.hms.pps.** { *; }


#买量sdk混淆配置===BEGIN=========
-keep  class com.cs.bd.buychannel.BuyChannelApi{*;}
-keep  class com.cs.bd.buychannel.BuySdkInitParams{*;}
-keep  class com.cs.bd.buychannel.BuySdkInitParams$Builder{*;}
-keep  interface com.cs.bd.buychannel.BuySdkInitParams$IProtocal19Handler{*;}
-keep  class com.cs.bd.buychannel.MPSharedPreferences{*;}
-keep  class com.cs.bd.ad.**{*;}
-keep  class com.cs.bd.utils.**{*;}
-keep  class com.cs.bd.buychannel.buyChannel.database.BuychannelDbHelpler.**{*;}
#买量sdk混淆配置===END===========


#*******************AndroidX
-keep class com.google.android.material.** {*;}

-keep class androidx.** {*;}

-keep public class * extends androidx.**

-keep interface androidx.** {*;}

-dontwarn com.google.android.material.**

-dontnote com.google.android.material.**

-dontwarn androidx.**

#apk更新
#在混淆文件中添加规则，不混淆sdk
-keep class com.base.services.**{*;}
#//若混淆资源文件的时候，注意不要混淆公钥密钥文件micro_services_config.json，否则会报找不到文件错误。

#************************jPush********************
-dontoptimize
-dontpreverify
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }
#==================gson && protobuf==========================
-dontwarn com.google.**
-keep class com.google.gson.** {*;}
-keep class com.google.protobuf.** {*;}
#************************jPush********************
#openAdSdk===START=========
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.ss.sys.ces.* {*;}
#openAdSdk===END===========

#友盟
-keep class com.umeng.** {*;}

#GDTSdk=====START==========
-keep class com.qq.e.** { public protected *; }
-keep class android.support.v4.**{ public *; }
-keep class android.support.v7.**{ public *; }
#GDTSdk=====END==========

#==================bugly==========================
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

#*************************项目配置**************
-keep class com.nft.quizgame.net.bean.**{*;}
-dontwarn com.cs.statistic.**
-keep class com.cs.statistic.**{*;}

-keep interface  com.nft.quizgame.data.IDataBase
-keep class * implements com.nft.quizgame.data.IDataBase{*;}

-keep class com.nft.quizgame.function.quiz.BaseQuizViewModel
-keep class com.nft.quizgame.function.quiz.QuizViewModuleParam
-keep class * extends com.nft.quizgame.function.quiz.BaseQuizViewModel {
    public <init>();
     public <init>(com.nft.quizgame.function.quiz.QuizViewModuleParam);
}

