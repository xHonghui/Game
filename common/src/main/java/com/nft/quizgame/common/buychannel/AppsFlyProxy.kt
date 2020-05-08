package com.nft.quizgame.common.buychannel

import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.appsflyer.AppsFlyerLib
import com.cs.bd.buychannel.BuySdkConstants
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yangjiacheng on 2017/10/10.
 * ...
 */
object AppsFlyProxy {

    val TAG = "AppsFlyer"

    fun trackEvent(eventName: String?, eventValues: Map<String?, Any?>?) {
        AppsFlyerLib.getInstance().trackEvent(QuizAppState.getContext(), eventName, eventValues)
    }

    private fun realUploadNextDayRetain(mApplication: Context?) {
        AppsFlyerLib.getInstance().trackEvent(mApplication, "af_next_day_retain", HashMap())
        AppsFlyerLib.getInstance().reportTrackSession(mApplication)
    }

    private fun realUploadNextDayOpenRetain(mApplication: Context?) {
        AppsFlyerLib.getInstance().trackEvent(mApplication, "af_next_day_open_retain", HashMap())
        AppsFlyerLib.getInstance().reportTrackSession(mApplication)
    }

    fun uploadRewardVideoDone() {
        AppsFlyerLib.getInstance().trackEvent(QuizAppState.getContext(), "reward_video_done", HashMap())
        AppsFlyerLib.getInstance().reportTrackSession(QuizAppState.getContext())
    }

    fun uploadQuizDone50() {
        AppsFlyerLib.getInstance().trackEvent(QuizAppState.getContext(), "quiz_done_50", HashMap())
        AppsFlyerLib.getInstance().reportTrackSession(QuizAppState.getContext())
    }

    fun uploadNextDayOpenRetain(context: Application, firstRunTime: Long) {
        val preference = PrivatePreference.getPreference()
        val uploadNextDayOpenRetain: Boolean = preference.getValue(PrefConst.KEY_AF_NEXT_DAY_OPEN_RETAIN_UPLOAD, false)
        if (uploadNextDayOpenRetain) {
            return
        }
        if (!isAppInstallToday(firstRunTime) && !isAppInstallTomorrow(firstRunTime)) {
            return
        }

        context.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacksAdapter() {
            override fun onActivityResumed(activity: Activity) {
                super.onActivityResumed(activity)
                if (preference.getValue(PrefConst.KEY_AF_NEXT_DAY_OPEN_RETAIN_UPLOAD, false)) {
                    context.unregisterActivityLifecycleCallbacks(this)
                    return
                }
                if (isAppInstallToday(firstRunTime)) {
                    return
                }

                if (isAppInstallTomorrow(firstRunTime)) {
                    //上传统计
                    preference.putValue(PrefConst.KEY_AF_NEXT_DAY_OPEN_RETAIN_UPLOAD, true).apply()
                    realUploadNextDayOpenRetain(context)
                }

                //其他时间段直接不监听
                context.unregisterActivityLifecycleCallbacks(this)
            }
        })
    }

    fun uploadNextDayKeep(context: Context, firstRunTime: Long) {
        val preference = PrivatePreference.getPreference()
        val uploadNextDayKeep: Boolean = preference.getValue(PrefConst.KEY_NEXT_DAY_KEEP_UPLOAD, false)
        Logcat.d(TAG, "uploadNextDayKeep = $uploadNextDayKeep")
        if (!uploadNextDayKeep) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            val instance = Calendar.getInstance()
            instance.time = Date(firstRunTime)
            instance[Calendar.DAY_OF_YEAR] = instance[Calendar.DAY_OF_YEAR] + 1
            instance[Calendar.HOUR_OF_DAY] = 0
            instance[Calendar.MINUTE] = 0
            instance[Calendar.SECOND] = 0
            instance[Calendar.MILLISECOND] = 0
            val currentTimeFormat = simpleDateFormat.format(Date(System.currentTimeMillis()))
            val firstRunTimeFormat = simpleDateFormat.format(Date(firstRunTime))
            if (currentTimeFormat != firstRunTimeFormat) {
                Logcat.d(TAG, "currentTimeFormat = $currentTimeFormat, firstRunTimeFormat = $firstRunTimeFormat")
                if (currentTimeFormat == simpleDateFormat.format(instance.time)) {
                    processUploadNextDayKeep()
                } else {
                    Logcat.d(TAG, "不是次日")
                }
            } else {
                val intentFilter = IntentFilter()
                intentFilter.addAction("upload_next_day_keep")
                context.registerReceiver(UploadNextDayKeepReceiver(), intentFilter)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val triggerAtMillis = instance.time.time - firstRunTime
                Logcat.d(TAG, "当天 triggerAtMillis = $triggerAtMillis")
                val updateIntent = Intent("upload_next_day_keep")
                val pendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
                AppUtils.triggerAlarm(alarmManager, AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + triggerAtMillis, pendingIntent)
            }
        }
    }


    fun isAppInstallToday(firstRunTime: Long): Boolean {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val currentTimeFormat = simpleDateFormat.format(Date(System.currentTimeMillis()))
        val firstRunTimeFormat = simpleDateFormat.format(Date(firstRunTime))
        return currentTimeFormat == firstRunTimeFormat
    }

    fun isAppInstallTomorrow(firstRunTime: Long): Boolean {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val nextDay = Calendar.getInstance()
        nextDay.time = Date(firstRunTime)
        nextDay[Calendar.DAY_OF_YEAR] = nextDay[Calendar.DAY_OF_YEAR] + 1

        val currentTimeFormat = simpleDateFormat.format(Date(System.currentTimeMillis()))
        return currentTimeFormat == simpleDateFormat.format(nextDay.time)
    }


    private fun processUploadNextDayKeep() {
        val pref: PrivatePreference = PrivatePreference.getPreference()
        val uploadNextDayKeep: Boolean = pref.getValue(PrefConst.KEY_NEXT_DAY_KEEP_UPLOAD, false)
        if (!uploadNextDayKeep) {
            Logcat.d(TAG, "uploadNextDayKeep")
            PrivatePreference.getPreference().putValue(PrefConst.KEY_NEXT_DAY_KEEP_UPLOAD, true).apply()
            realUploadNextDayRetain(QuizAppState.getContext())
        }
    }


    private class UploadNextDayKeepReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            processUploadNextDayKeep()
        }
    }
}