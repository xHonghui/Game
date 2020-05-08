package com.nft.quizgame.common.ad

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.bytedance.sdk.openadsdk.activity.TTFullScreenVideoActivity
import java.lang.ref.WeakReference

class ActivityLifecycleCallbackAdAdapter : ActivityLifecycleCallbacks {
    val result: Activity?
        get() = if (mResult != null) {
            mResult!!.get()
        } else null

    private var mResult: WeakReference<Activity>? = null
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is TTFullScreenVideoActivity) {
            mResult = WeakReference(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
    }

    override fun onActivityDestroyed(activity: Activity) {}
}