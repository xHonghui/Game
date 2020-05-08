package com.nft.quizgame

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Looper
import android.os.Parcelable
import android.os.SystemClock
import android.util.SparseArray
import com.nft.quizgame.common.ICustomAction
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq19OperationStatistic
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.common.utils.Machine
import com.nft.quizgame.config.ConfigManager
import com.nft.quizgame.ext.post
import com.nft.quizgame.ext.postDelayed
import com.nft.quizgame.ext.removeCallbacks
import com.nft.quizgame.function.update.AppUpdateManger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.*

/**
 * Created by kingyang on 2017/2/28.
 */
class ScheduleTaskManager private constructor() : BroadcastReceiver() {
    private val mAlarmManager: AlarmManager
    private val mWifiMgr: WifiManager
    private val mContext: Context = QuizAppState.getContext()
    private val mPendingTasks =
            ArrayList<ScheduleTask>()
    private val mFactory = ScheduleTaskFactory()
    private val taskContext = newSingleThreadContext("schedule_task_thread")

    companion object {
        const val TAG = "ScheduleTaskManager"
        private var sInstance: ScheduleTaskManager? = null
        val instance: ScheduleTaskManager
            get() {
                if (sInstance == null) {
                    sInstance = ScheduleTaskManager()
                }
                return sInstance!!
            }

        const val TASK_ID_BASE_DATA_UPLOAD = 1
        const val TASK_ID_APP_UPDATE = 2
        const val TASK_ID_AB_CONFIG_REFRESH = 3
    }

    init {
        mAlarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mWifiMgr = mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        val tasks: SparseArray<ScheduleTask> = mFactory.getAllScheduleTasks()
        for (i in 0 until tasks.size()) {
            val task = tasks[tasks.keyAt(i)]
            filter.addAction(task.action)
        }


        mContext.registerReceiver(this, filter)
    }


    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ConnectivityManager.CONNECTIVITY_ACTION == action) {
            val networkInfo = intent
                    .getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO)
            if (networkInfo is NetworkInfo) {
                val type = networkInfo.type
                val state = networkInfo.state
                if (type == ConnectivityManager.TYPE_MOBILE) {
                    if (state == NetworkInfo.State.CONNECTED) {
                        startPendingTasks()
                    }
                }
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION == action) {
            val bundle = intent.extras ?: return
            val wifiInfo = mWifiMgr.connectionInfo
            val parcelableExtra = intent
                    .getParcelableExtra<Parcelable>(WifiManager.EXTRA_NETWORK_INFO)
            if (parcelableExtra is NetworkInfo) {
                val networkInfo = parcelableExtra
                val state = networkInfo.state
                if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    if (wifiInfo != null && state == NetworkInfo.State.CONNECTED && wifiInfo.supplicantState == SupplicantState.COMPLETED) {
                        startPendingTasks()
                    }
                }
            }
        } else {
            val taskMap: SparseArray<ScheduleTask> = mFactory.getAllScheduleTasks()
            for (i in 0 until taskMap.size()) {
                val key = taskMap.keyAt(i)
                val task = taskMap[key]
                if (task.onReceive(action)) {
                    break
                }
            }
        }
    }

    private fun startPendingTasks() {
        if (!Machine.isNetworkOK(mContext)) {
            return
        }

        GlobalScope.launch(taskContext) {
            for (task in mPendingTasks) {
                launch(Dispatchers.Main) {
                    task.restart()
                }
                SystemClock.sleep(1000)
            }
            mPendingTasks.clear()
        }
    }

    private fun addPendingTask(task: ScheduleTask) {
        GlobalScope.launch(taskContext) {
            for (t in mPendingTasks) {
                if (t.id == task.id) { //已经添加，返回
                    return@launch
                }
            }
            mPendingTasks.add(task)
        }
    }

    abstract inner class ScheduleTask(val id: Int, private val interval: Long, val action: String) {

        private var mPendingIntent: PendingIntent? = null
        private var mStartRunnable: Runnable? = null
        private val checkedTimeKey: String = "key_checked_time_$id"

        fun start(delay: Long) {
            if (mStartRunnable == null) {
                mStartRunnable =
                        Runnable { startIntervalTask(false, interval, action) }
            }
            mStartRunnable?.let {
                postDelayed(delay, it)
            }
        }

        fun action() {
            if (needNetwork() && !Machine.isNetworkOK(mContext)) {
                addPendingTask(this)
            } else {
                if (doAction()) {
                    startIntervalTask(true, interval, action)
                }
            }
        }

        fun restart() {
            if (doAction()) {
                startIntervalTask(true, interval, action)
            }
        }

        fun cancel() {
            mStartRunnable?.let {
                removeCallbacks(it)
            }
            if (mPendingIntent != null) {
                mAlarmManager.cancel(mPendingIntent)
            }
        }

        fun onReceive(action: String?): Boolean {
            if (this.action == action) {
                action()
                return true
            }
            return false
        }

        /**
         * 间隔指定时间执行任务
         *
         * @param fromReceiver   是否从Receiver回调
         * @param interval       任务间隔
         * @param action
         */
        fun startIntervalTask(fromReceiver: Boolean, interval: Long, action: String?) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                doStartIntervalTask(fromReceiver, interval, action)
            } else {
                post { doStartIntervalTask(fromReceiver, interval, action) }
            }
        }

        private fun doStartIntervalTask(fromReceiver: Boolean, interval: Long, action: String?) {
            try {
                val now = System.currentTimeMillis()
                if (fromReceiver) {
                    setLastCheckedTime(now)
                }
                var toNextIntervalTime: Long = 0 // 下一次上传间隔时间
                val lastCheckUpdate = getLastCheckedTime() // 上一次的检查时间
                when {
                    lastCheckUpdate == 0L -> {
                    }
                    now - lastCheckUpdate >= interval -> {
                    }
                    else -> { // 动态调整下一次的间隔时间
                        toNextIntervalTime = interval - (now - lastCheckUpdate)
                    }
                }
                if (toNextIntervalTime == 0L) {
                    val updateIntent = Intent(action)
                    mContext.sendBroadcast(updateIntent)
                } else {
                    val triggerTime = System.currentTimeMillis() + toNextIntervalTime
                    if (mPendingIntent == null) {
                        val updateIntent = Intent(action)
                        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    }
                    AppUtils.triggerAlarm(mAlarmManager, AlarmManager.RTC_WAKEUP, triggerTime, mPendingIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun setLastCheckedTime(checkedTime: Long) {
            val pref = PrivatePreference.getPreference()
            pref.putValue(checkedTimeKey, checkedTime)
            pref.apply()
        }

        private fun getLastCheckedTime(): Long {
            val pref = PrivatePreference.getPreference()
            return pref.getValue(checkedTimeKey, 0L)
        }

        /**
         * 该任务要做的事情全写在这个方法里
         */
        abstract fun doAction(): Boolean

        /**
         * 该任务是否需要网络
         *
         * @return
         */
        abstract fun needNetwork(): Boolean

    }

    fun cancelTask(taskId: Int) {
        val task = mFactory.getScheduleTask(taskId)
        task?.cancel()
    }

    /**
     * 强制执行Task
     */
    fun startTask(taskId: Int) {
        mFactory.getScheduleTask(taskId)?.action()
    }

    fun startScheduleTasks() {
        mFactory.getScheduleTask(TASK_ID_BASE_DATA_UPLOAD)?.start(0)
        mFactory.getScheduleTask(TASK_ID_APP_UPDATE)?.start(0)
        mFactory.getScheduleTask(TASK_ID_AB_CONFIG_REFRESH)?.start(0)
    }

    inner class ScheduleTaskFactory {

        private val mTaskMap = SparseArray<ScheduleTask>()

        init {
            initTasks()
        }

        private fun initTasks() { //在startTasks，启动任务
            var task = createBaseDataUploadTask()
            mTaskMap.put(task.id, task)
            task = createAppUpdateConfigTask()
            mTaskMap.put(task.id, task)
            task = createAbConfigRefreshTask()
            mTaskMap.put(task.id, task)
        }

        fun getScheduleTask(taskId: Int): ScheduleTask? {
            return mTaskMap.get(taskId)
        }

        fun getAllScheduleTasks(): SparseArray<ScheduleTask> {
            return mTaskMap.clone()
        }

        private fun createBaseDataUploadTask(): ScheduleTask {
            return object : ScheduleTask(TASK_ID_BASE_DATA_UPLOAD, AlarmManager.INTERVAL_HOUR * 8,
                    ICustomAction.ACTION_UPLOAD_BASIC_STATISTIC) {
                override fun doAction(): Boolean {
                    BaseSeq19OperationStatistic.uploadBasicInfo()
                    return true
                }

                override fun needNetwork(): Boolean {
                    return true
                }
            }
        }

        private fun createAppUpdateConfigTask(): ScheduleTask {
            return object : ScheduleTask(TASK_ID_APP_UPDATE, AlarmManager.INTERVAL_HOUR * 8,
                    ICustomAction.ACTION_APP_UPDATE_CONFIG) {
                override fun doAction(): Boolean {
                    Logcat.i(TAG, "requestAppUpdateConfigTask")
                    GlobalScope.launch {
                        try {
                            AppUpdateManger.checkAppUpdate()
                            Logcat.i(TAG, "requestAppUpdateConfigTask success")
                            startIntervalTask(true, AlarmManager.INTERVAL_HOUR * 8,
                                    ICustomAction.ACTION_APP_UPDATE_CONFIG)
                        } catch (e: Exception) {
                            Logcat.i(TAG, "requestAppUpdateConfigTask fail")
                            startIntervalTask(true, 5 * 60 * 1000L, ICustomAction.ACTION_APP_UPDATE_CONFIG)
                        }
                    }
                    return false
                }

                override fun needNetwork(): Boolean {
                    return true
                }
            }
        }

        private fun createAbConfigRefreshTask(): ScheduleTask {
            //cacheManager设置了缓存失效时间为8小时，这里1小时检查一次，避免上次缓存没有刷新
            return object : ScheduleTask(TASK_ID_AB_CONFIG_REFRESH, AlarmManager.INTERVAL_HOUR,
                    ICustomAction.ACTION_AB_CONFIG_REFRESH) {
                override fun doAction(): Boolean {
                    ConfigManager.getInstance().loadAllConfigs()
                    return true
                }

                override fun needNetwork(): Boolean {
                    return true
                }
            }
        }

    }

}