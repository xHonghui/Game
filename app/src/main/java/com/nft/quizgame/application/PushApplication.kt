package com.nft.quizgame.application

import com.nft.quizgame.function.push.PushManager

class PushApplication(processName: String?) : BaseApplication(processName) {

    override fun onCreate() {
        super.onCreate()
        PushManager.init(this)
        PushManager.setTag(this)
    }

    override fun isMainProcess(): Boolean {
        return false
    }
}