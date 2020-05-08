package com.nft.quizgame.application

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.SystemClock
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.common.IApplication
import com.nft.quizgame.common.QuizAppFacade
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.QuizEnv
import com.nft.quizgame.common.buychannel.BuyChannelApiProxy
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.crash.CrashHandler
import com.nft.quizgame.function.user.UserViewModel
import com.nft.quizgame.version.VersionController
import com.tencent.bugly.crashreport.CrashReport

class ApplicationProxy : Application(), IApplication {

    private var mAppImpl: IApplication? = null

    private fun initAppImpl(context: Context?) {
        var processName: String? = null
        var retryCount = 0
        val maxRetry = 2
        while (processName == null) {
            processName = AppUtils.getCurProcessName(context)
            if (processName == null) {
                retryCount++
                if (retryCount > maxRetry) {
                    break
                }
                SystemClock.sleep(500)
            }
        }
        if (processName == null || QuizEnv.sProcessName == processName) {
            mAppImpl = QuizApplication(processName)
        } else if (QuizEnv.sProcessDaemonAssistant == processName) {
            mAppImpl = DaemonAssistantApp(processName)
        } else if (QuizEnv.sProcessPush == processName) {
            mAppImpl = PushApplication(processName)
        }

        if (mAppImpl == null) {
            mAppImpl = QuizApplication(processName)
        }
        Logcat.setEnable(BuildConfig.DEBUG)
        Logcat.i("Test", "processName: " + processName + " mAppImpl: ${mAppImpl!!::class.java.simpleName}")
    }

    override fun onCreate() {
        super.onCreate()
        initCrashHandler()
        BuyChannelApiProxy.preInit(this)
        mAppImpl?.onCreate()
    }

    override fun onTerminate() {
        super.onTerminate()
        mAppImpl?.onTerminate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mAppImpl?.onConfigurationChanged(newConfig)
    }

    override fun isMainProcess(): Boolean {
        return mAppImpl?.isMainProcess() ?: true
    }

    override fun getCustomProcessName(): String {
        return mAppImpl?.getCustomProcessName() ?: packageName
    }

    override fun stopSdk(stop: Boolean) {
        mAppImpl?.stopSdk(stop)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        QuizAppState.init(this, object : QuizAppFacade {

            override fun isFirstRun(): Boolean = VersionController.isFirstRun

            override fun getCDays(): Int = VersionController.cdays

            override fun getABUser(): String? = /*ABTest.getInstance().getUser()*/""

            override fun getUserAccessToken(): String? = AppViewModelProvider.getInstance().get(UserViewModel::class.java).getUserAccessToken()

            override fun getUserRefreshToken(): String? = AppViewModelProvider.getInstance().get(UserViewModel::class.java).getUserRefreshToken()
        })

        initAppImpl(base)
        mAppImpl?.attachBaseContext(base)
    }

    private fun initCrashHandler() {
        CrashHandler.init()
        if (!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(applicationContext, AppUtils.getBuglyAppId(applicationContext), false)
            CrashReport.setAppChannel(applicationContext, AppUtils.getBuglyChannel(applicationContext))
        }
    }
}