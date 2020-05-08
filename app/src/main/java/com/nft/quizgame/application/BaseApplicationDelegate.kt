package com.nft.quizgame.application

import android.app.Activity
import android.app.Application
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.cs.bd.ad.AdSdkApi
import com.cs.bd.ad.params.ClientParams
import com.cs.bd.buychannel.IBuyChannelUpdateListener
import com.cs.statistic.StatisticsManager
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.R
import com.nft.quizgame.common.IApplication
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.QuizEnv
import com.nft.quizgame.common.ad.AdController
import com.nft.quizgame.common.buychannel.BuyChannelApiProxy
import com.nft.quizgame.common.net.VolleyManager
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.DrawUtils
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.common.utils.WindowController
import com.nft.quizgame.version.VersionController
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.String

/**
 * App基本逻辑委托类
 *
 * @author yangguanxiang
 */
class BaseApplicationDelegate internal constructor(private val mApplication: Application) {

    fun onCreate() {
        DrawUtils.resetDensity(mApplication)
        VolleyManager.initContext(mApplication)
        VolleyManager.getInstance().start()
        WindowController.init(mApplication)
        initStatisticsManager()
        if (mApplication is IApplication && (mApplication as IApplication).isMainProcess()) {
            VersionController.init()
        }
        BuyChannelApiProxy.init(QuizAppState.getApplication())
    }

    private fun initStatisticsManager() { // 初始化统计
        StatisticsManager.initBasicInfo(QuizEnv.sProcessName, QuizEnv.sChannelId,
                arrayOf(mApplication.resources.getString(R.string.diff_statistics_host)), null)
        val excludeActivities: Array<Class<out Activity?>> = arrayOf<Class<out Activity>>()
        StatisticsManager.enableApplicationStateStatistic(QuizAppState.getApplication(), null, excludeActivities)
        StatisticsManager.registerCrashReporter { t -> CrashReport.postCatchedException(t) }
        val statisticsManager = StatisticsManager.getInstance(mApplication)
        statisticsManager.enableLog(BuildConfig.DEBUG)
        statisticsManager.setJobSchedulerEnable(true)
    }

    fun initAdSDK() {
        val application = mApplication
        TTAdSdk.init(application, AdController.ttAdConfig)

        val params = ClientParams(
            BuyChannelApiProxy.buyChannel,
            AppUtils.getAppFirstInstallTime(mApplication, mApplication.packageName),
            !VersionController.isNewUser
        )
        params.useFrom = String.valueOf(BuyChannelApiProxy.secondUserType)
        Logcat.d("UseFrom", String.valueOf(BuyChannelApiProxy.secondUserType))
        AdSdkApi.setClientParams(application, params)
        AdSdkApi.setEnableLog(BuildConfig.DEBUG)
        AdSdkApi.initSDK(
            application,
            mApplication.packageName,
            StatisticsManager.getUserId(application),
            "",
            AppUtils.getChannel(application),
            null
        )


        BuyChannelApiProxy.registerBuyChannelUpdateListener(IBuyChannelUpdateListener {
            GlobalScope.launch (Dispatchers.IO){
                val appFirstInstallTime: Long =
                    AppUtils.getAppFirstInstallTime(mApplication, mApplication.packageName)
                val clientParams = ClientParams(
                    BuyChannelApiProxy.buyChannel,
                    appFirstInstallTime,
                    !VersionController.isNewUser
                )
                clientParams.useFrom = BuyChannelApiProxy.secondUserType.toString()
                AdSdkApi.setClientParams(mApplication, clientParams)
                Logcat.d("UseFrom", String.valueOf(BuyChannelApiProxy.secondUserType))
            }
        })
    }
}