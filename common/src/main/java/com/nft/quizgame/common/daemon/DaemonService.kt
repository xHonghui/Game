package com.nft.quizgame.common.daemon

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.cs.bd.daemon.DaemonClient
import com.cs.bd.daemon.NotificationAssistService
import com.nft.quizgame.common.ICustomAction
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference.Companion.getPreference
import com.nft.quizgame.common.statistic.BaseSeq19OperationStatistic
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by kingyang on 2016/8/8.
 */
class DaemonService : Service() {
    private var mReceiver: BroadcastReceiver? = null
    private var mAlarmManager: AlarmManager? = null
    override fun onCreate() {
        super.onCreate()
        //设置为前台服务，降低被杀几率。参数里的两个服务必须配置在同一进程内。
//        DaemonClient.getInstance().setForgroundService(this, InnerDaemonService.class);
        init()
        startTask()
        Logcat.i(TAG, "DaemonService onCreate")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Logcat.i(TAG, "DaemonService onStartCommand")
        //统计守护效果
        DaemonClient.getInstance().statisticsDaemonEffect(this, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
        Logcat.i(TAG, "DaemonService onDestroy")
    }

    private fun init() {
        mAlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == ICustomAction.ACTION_UPLOAD_BASIC_STATISTIC) {
                    startBasicInfoStaticTask(true)
                }
            }
        }
        val filter = IntentFilter()
        filter.addAction(ICustomAction.ACTION_UPLOAD_BASIC_STATISTIC)
        registerReceiver(mReceiver, filter)
    }

    private fun startTask() {
        startBasicInfoStaticTask(false)
    }

    /**
     * <br></br>
     * 功能简述:协议19 <br></br>
     */
    private fun startBasicInfoStaticTask(fromReceiver: Boolean) {
        try {
            val now = System.currentTimeMillis()
            val lastCheckUpdate = getLastCheckedTime(PrefConst.KEY_UPLOAD_BASIC_INFO_CHECK_TIME) // 上一次的检查时间
            val triggerTime = if (lastCheckUpdate == 0L) {
                doStartUploadBasicInfoStatic(now)
                now + EIGHT_HOURS
            } else {
                if (fromReceiver) {
                    doStartUploadBasicInfoStatic(now)
                    now + EIGHT_HOURS
                } else {
                    if (now - lastCheckUpdate >= TWO_HOURS) {
                        doStartUploadBasicInfoStatic(now)
                        now + EIGHT_HOURS
                    } else {
                        lastCheckUpdate + TWO_HOURS
                    }
                }
            }
            val updateIntent = Intent(ICustomAction.ACTION_UPLOAD_BASIC_STATISTIC)
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, updateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            AppUtils.triggerAlarm(mAlarmManager, AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun doStartUploadBasicInfoStatic(now: Long) {
        GlobalScope.launch {
            Logcat.i(TAG, "doStartUploadBasicInfoStatic")
            BaseSeq19OperationStatistic.uploadBasicInfo()
            // 保存本次检查的时长
            setLastCheckedTime(PrefConst.KEY_UPLOAD_BASIC_INFO_CHECK_TIME, now)
        }
    }

    private fun getLastCheckedTime(key: String): Long {
        val pref = getPreference()
        return pref.getValue(key, 0L)
    }

    private fun setLastCheckedTime(key: String, checkedTime: Long) {
        val pref = getPreference()
        pref.putValue(key, checkedTime).apply()
    }

    /**
     * 内部服务，用于设置前台进程
     */
    class InnerDaemonService : NotificationAssistService()

    companion object {
        private const val TAG = "DaemonService"
        private const val EIGHT_HOURS = 8 * 60 * 60 * 1000.toLong()// 每隔8小时
        private const val TWO_HOURS = 2 * 60 * 60 * 1000.toLong()// 每隔2小时
        private const val ONE_MINUTE = 60 * 1000.toLong() // 每隔1分钟
    }
}