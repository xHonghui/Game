package com.nft.quizgame.common

import android.content.Context
import com.nft.quizgame.common.utils.AppUtils
import java.io.File

object QuizEnv {
    val sProcessName: String = QuizAppState.getApplication().packageName
    val sProcessDaemonAssistant: String = "$sProcessName:daemonAssistant"
    val sProcessPush: String = "$sProcessName:pushcore"

    val sChannelId: String = AppUtils.getChannel(QuizAppState.getApplication())

    class Path {
        companion object {
            const val LOG_PATH = "logs"
            val sdcardPath = QuizAppState.getContext().getExternalFilesDir(null)?.absolutePath?: getInnerFilePath(QuizAppState.getContext(), "storage")

            fun getInnerFilePath(context: Context, targetFilePath: String): String {
                var root = targetFilePath
                var target = ""
                if (targetFilePath.contains(File.separator)) {
                    val split = targetFilePath.split(File.separator, limit = 2).toTypedArray()
                    root = split[0]
                    target = split[1]
                }
                return File(context.getDir(root, Context.MODE_PRIVATE), target).absolutePath
            }
        }
    }
}