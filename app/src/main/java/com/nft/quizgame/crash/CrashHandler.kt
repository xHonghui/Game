package com.nft.quizgame.crash

import android.content.Intent
import android.util.Log
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.QuizEnv
import com.nft.quizgame.common.utils.FileUtils
import com.tencent.bugly.crashreport.CrashReport
import java.io.File

class CrashHandler : Thread.UncaughtExceptionHandler {

    companion object {
        fun init() {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        }
    }

    private constructor()

    override fun uncaughtException(thread: Thread, e: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("Crash", Log.getStackTraceString(e))
            saveExceptionToLog(e)
        } else {
            CrashReport.postCatchedException(e)
        }
        val intent = Intent(QuizAppState.getContext(), RestartService::class.java)
        QuizAppState.getContext().startService(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun saveExceptionToLog(e: Throwable) {
        val exceptionContent = Log.getStackTraceString(e)
        val file = File(QuizEnv.Path.sdcardPath + File.separator + QuizEnv.Path.LOG_PATH,
                "crash_${System.currentTimeMillis()}.txt")
        FileUtils.writeString(file, exceptionContent)
    }

}