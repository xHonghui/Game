package com.nft.quizgame.application

import android.content.Intent
import android.content.IntentFilter
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.ScheduleTaskManager
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.buychannel.AppsFlyProxy
import com.nft.quizgame.common.download.DownloadProxy
import com.nft.quizgame.common.download.FileDownloaderStrategy
import com.nft.quizgame.external.ExternalDialogBroadcastReceiver
import com.nft.quizgame.function.push.PushManager
import com.nft.quizgame.function.quiz.QuizPropertyViewModel
import com.nft.quizgame.function.update.AppUpdateManger
import com.nft.quizgame.function.user.UserViewModel
import com.nft.quizgame.version.VersionController


class QuizApplication(processName: String?) : BaseApplication(processName) {

    override fun onCreate() {
        super.onCreate()
//        DaemonSdkProxy.init(applicationContext)
//        DaemonSdkProxy.startDaemonService(applicationContext)

        mDelegate.initAdSDK()

        ScheduleTaskManager.instance.startScheduleTasks()

        //初始化User
        AppViewModelProvider.getInstance().get(UserViewModel::class.java)
        //初始化QuizProperty
        AppViewModelProvider.getInstance().get(QuizPropertyViewModel::class.java)

        PushManager.init(this)

        val externalDialogBroadcastReceiver = ExternalDialogBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(externalDialogBroadcastReceiver, intentFilter)

        AppUpdateManger.init()
        DownloadProxy.init(this, FileDownloaderStrategy())

        AppsFlyProxy.uploadNextDayKeep(QuizAppState.getContext(), VersionController.getFirstRunTime())
        AppsFlyProxy.uploadNextDayOpenRetain(QuizAppState.getApplication(),VersionController.getFirstRunTime())
    }


    override fun isMainProcess(): Boolean {
        return true
    }
}