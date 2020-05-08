package com.nft.quizgame.version

import com.nft.quizgame.common.QuizAppState.getContext
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq19OperationStatistic
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.common.utils.Logcat
import kotlin.math.roundToInt

/**
 * @author yangguanxiang
 */
object VersionController {
    private const val TIME_UNIT_SECOND = 1001
    private const val TIME_UNIT_MIN = 1002
    private const val TIME_UNIT_HOUR = 1003
    private const val TIME_UNIT_DAY = 1004
    //安装桌面首次运行
    //	private boolean mHadPayFlag = false; // 是否已经付费
    var isFirstRun = false
        private set
    /**
     * 获取上一个版本的versionCode，只在安装或更新桌面首次运行时生效，否则与当前versionCode相同
     *
     * @return
     */
    var lastVersionCode = 0
        private set
    private var sCurrentVersionCode = -1
    /**
     * 安装或更新桌面首次运行
     *
     * @return
     */
    var isNewVersionFirstRun = false
        private set
    private var sIsNewUser: Boolean? = null
    private var sIsInited = false

    /**
     * 获取当前versionCode
     *
     * @return
     */
    val currentVersionCode: Int
        get() {
            if (sCurrentVersionCode == -1) {
                val context = getContext()
                sCurrentVersionCode = AppUtils.getVersionCodeByPkgName(context, context.packageName)
            }
            return sCurrentVersionCode
        }

    @JvmStatic
    fun init() {
        if (sIsInited) {
            return
        }
        checkFirstRun()
        if (!isFirstRun) {
            checkNewVersionFirstRun()
        } else {
            onFirstRun()
        }
        if (isNewVersionFirstRun) {
            onNewVersionFirstRun()
        }
        sIsInited = true
        Logcat.i("VersionController", "sFirstRun: $isFirstRun")
        Logcat.i("VersionController", "sNewVersionFirstRun: $isNewVersionFirstRun")
        Logcat.i("VersionController", "sIsNewUser: $isNewUser")
        Logcat.i("VersionController", "sLastVersionCode: $lastVersionCode")
        Logcat.i("VersionController", "sCurrentVersionCode: $currentVersionCode")
    }

    /**
     * 检测是否为第一次运行
     */
    private fun checkFirstRun() {
        val preference = PrivatePreference.getPreference()
        isFirstRun = preference.getValue(PrefConst.KEY_LAST_VERSION_CODE, -1) == -1
    }

    private fun onFirstRun() {
        val pref = PrivatePreference.getPreference()
        pref.putValue(PrefConst.KEY_LAST_VERSION_CODE, currentVersionCode)
            .putValue(PrefConst.KEY_FIRST_RUN_TIME, System.currentTimeMillis()).apply()
        isNewVersionFirstRun = true
        saveIsNewUserPref(true)
    }

    /**
     * <br></br>功能简述:检查是否是该版本第一次运行
     * <br></br>功能详细描述:
     * <br></br>注意:
     */
    private fun checkNewVersionFirstRun() {
        val pref = PrivatePreference.getPreference()
        lastVersionCode = pref.getValue(PrefConst.KEY_LAST_VERSION_CODE, 0)
        val curVersionCode = currentVersionCode
        if (curVersionCode != -1 && curVersionCode != lastVersionCode) {
            isNewVersionFirstRun = true
            pref.putValue(PrefConst.KEY_LAST_VERSION_CODE, curVersionCode).apply()
        }
    }

    private fun onNewVersionFirstRun() {
        val pref = PrivatePreference.getPreference()
        if (lastVersionCode > 0) {
            // 如果上次版本号大于0，证明该用户是升级用户，如果是全新用户，上次版本号为0
            // 这里保存该用户不是新用户，因为上次版本号不是0，已经运行过了
            saveIsNewUserPref(false)
            BaseSeq19OperationStatistic.uploadBasicInfo()
        }
    }

    //是否是全新用户
    val isNewUser: Boolean
        get() {
            if (sIsNewUser == null) {
                val sharedPreferences = PrivatePreference.getPreference()
                sIsNewUser =
                    sharedPreferences.getValue(PrefConst.KEY_IS_NEW_USER, true)
            }
            return sIsNewUser!!
        }

    //记录当前用户是否是全新用户
    private fun saveIsNewUserPref(isNewUser: Boolean) {
        sIsNewUser = isNewUser
        val sharedPreferences = PrivatePreference.getPreference()
        sharedPreferences.putValue(PrefConst.KEY_IS_NEW_USER, isNewUser).apply()
    }

    val cdays: Int
        get() {
            var cdays = 1
            val pref = PrivatePreference.getPreference()
            val firstRunTime = pref.getValue(PrefConst.KEY_FIRST_RUN_TIME, 0L)
            if (firstRunTime > 0) {
                val diff = System.currentTimeMillis() - firstRunTime
                cdays = (diff / 1000 / 86400.toFloat()).roundToInt()
                if (cdays < 1) {
                    cdays = 1
                } else {
                    cdays += 1
                }
                Logcat.d("xiaowu_install", "cday: " + cdays + " diff: " + diff / 1000 / 86400)
            }
            return cdays
        }

    fun getFirstRunTime(): Long {
        val pref = PrivatePreference.getPreference()
        return pref.getValue(PrefConst.KEY_FIRST_RUN_TIME, 0L)
    }

    /**
     * 获取具体首次启动的时间
     *
     * @param timeUnit 时间类型：秒、分、时、天
     * @return 相应类型的时间间隔
     */
    fun getFirstRunInterval(timeUnit: Int): Float {
        var interval = 0f
        var time = 1
        val pref = PrivatePreference.getPreference()
        val firstRunTime = pref.getValue(PrefConst.KEY_FIRST_RUN_TIME, 0L)
        if (firstRunTime > 0) {
            val diff = System.currentTimeMillis() - firstRunTime
            when (timeUnit) {
                TIME_UNIT_SECOND -> time = 1
                TIME_UNIT_MIN -> time = 60
                TIME_UNIT_HOUR -> time = 3600
                TIME_UNIT_DAY -> time = 86400
                else -> {
                }
            }
            interval = diff / 1000 / time.toFloat()
        }
        return interval
    }
}