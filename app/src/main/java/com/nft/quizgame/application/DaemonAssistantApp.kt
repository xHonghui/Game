package com.nft.quizgame.application

import android.content.Context
import com.nft.quizgame.common.daemon.DaemonSdkProxy

/**
 * 守护进程App
 *
 * @author yangguanxiang
 */
class DaemonAssistantApp(processName: String?) : BaseApplication(processName) {

    override fun isMainProcess(): Boolean {
        return false
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        DaemonSdkProxy.init(base)
    }

    override fun stopSdk(stop: Boolean) {
        DaemonSdkProxy.disableDaemon(applicationContext)
    }
}