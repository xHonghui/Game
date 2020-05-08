package com.nft.quizgame.common

import android.app.Application.ActivityLifecycleCallbacks
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources

interface IApplication {
    fun attachBaseContext(base: Context?)

    fun onCreate()

    fun onTerminate()

    fun getResources(): Resources?

    fun getApplicationContext(): Context?

    fun onConfigurationChanged(newConfig: Configuration)

    fun onLowMemory()

    fun onTrimMemory(level: Int)

    fun registerComponentCallbacks(callback: ComponentCallbacks?)

    fun unregisterComponentCallbacks(callback: ComponentCallbacks?)

    fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?)

    fun unregisterActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?)

    fun isMainProcess(): Boolean

    fun getCustomProcessName(): String?

    fun stopSdk(stop: Boolean)
}