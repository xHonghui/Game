package com.nft.quizgame.common.daemon

import android.content.Context
import android.content.Intent
import com.cs.bd.daemon.DaemonClient
import com.cs.bd.daemon.DaemonConfigurations
import com.cs.bd.daemon.DaemonConfigurations.DaemonConfiguration
import com.nft.quizgame.common.BuildConfig
import com.nft.quizgame.common.QuizEnv

/**
 * Created by kingyang on 2016/8/8.
 */
object DaemonSdkProxy {

    fun init(context: Context?) {
        //重要：以下接入代码，建议在业务代码之前执行，守护效果更好
        //设置测试模式，打开LOG
        if (BuildConfig.DEBUG) {
            DaemonClient.getInstance().setDebugMode()
        }
        //初始化DaemonClient
        DaemonClient.getInstance().init(createDaemonConfigurations())
        DaemonClient.getInstance().onAttachBaseContext(context)
    }

    /**
     * 构建守护配置
     *
     * @return
     */
    private fun createDaemonConfigurations(): DaemonConfigurations { //构建被守护进程配置信息
        val configuration1 = DaemonConfiguration(
            QuizEnv.sProcessName, DaemonService::class.java.canonicalName,
            DaemonReceiver::class.java.canonicalName)
        //构建辅助进程配置信息
        val configuration2 = DaemonConfiguration(
            QuizEnv.sProcessDaemonAssistant,
            AssistantService::class.java.canonicalName,
            AssistantReceiver::class.java.canonicalName)
        //listener can be null
        val configs = DaemonConfigurations(configuration1, configuration2)
        //开启守护效果统计
        configs.isStatisticsDaemonEffect = true
        //设置唤醒常驻服务轮询时长
//        configs.setDaemonWatchInterval(60);
        return configs
    }

    fun startDaemonService(context: Context?) {
        try {
            context?.startService(Intent(context, DaemonService::class.java))
        } catch (e: Exception) { //OPPO手机在这里会崩溃
        }
    }

    fun enableDaemon(context: Context?) {
        DaemonClient.getInstance().setDaemonPermiiting(context, true)
    }

    fun disableDaemon(context: Context?) {
        DaemonClient.getInstance().setDaemonPermiiting(context, false)
        context?.stopService(Intent(context, DaemonService::class.java))
    }
}