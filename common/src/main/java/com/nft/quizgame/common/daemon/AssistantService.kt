package com.nft.quizgame.common.daemon

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.cs.bd.daemon.NotificationAssistService

/**
 * Created by kingyang on 2016/8/8.
 */
class AssistantService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    /**
     * 内部服务，用于设置前台进程
     */
    class InnerAssistantService : NotificationAssistService()
}