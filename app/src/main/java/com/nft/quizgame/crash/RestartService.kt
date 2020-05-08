package com.nft.quizgame.crash

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.nft.quizgame.ext.postDelayed

class RestartService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        postDelayed(1000) {
            restartApp()
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}