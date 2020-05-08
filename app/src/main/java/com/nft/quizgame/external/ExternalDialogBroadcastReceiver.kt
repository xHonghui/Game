package com.nft.quizgame.external

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.config.ConfigManager
import com.nft.quizgame.config.bean.ExternalDialogAdConfigBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExternalDialogBroadcastReceiver : BroadcastReceiver() {

    //1时段一 2时段二
    private var statisticPeriod = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        context ?: return

        val action = intent.action
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS != action) {
            return
        }

        val reason = intent.getStringExtra("reason")
        if ("homekey" != reason) {
            return
        }

        GlobalScope.launch(Dispatchers.Main) {

            val configBean = ConfigManager.getInstance().getConfigBean(
                    ExternalDialogAdConfigBean.SID) as ExternalDialogAdConfigBean
            if (!isMeetTime(configBean)) {
                return@launch
            }

            val preference = PrivatePreference.getPreference()

            val calendar = Calendar.getInstance(Locale.CHINA)


            val day = 24 * 60 * 60 * 1000

            var value =
                    preference.getValue(PrefConst.KEY_CLOSE_REMIND_RECENT, 0L) + 6 * day
            calendar.time = Date(value)
            calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    24,
                    0,
                    0
            )
            //七天后的时间
            if (calendar.time.time > System.currentTimeMillis()) {
                Logcat.d(ExternalDialogUtil.tag, "七天内不出")
                return@launch
            }

            value = preference.getValue(PrefConst.KEY_CLOSE_REMIND_TODAY, 0L)
            calendar.time = Date(value)
            calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    24,
                    0,
                    0
            )
            //当天后的时间
            if (calendar.time.time > System.currentTimeMillis()) {
                Logcat.d(ExternalDialogUtil.tag, "当天内不出")
                return@launch
            }


            //逻辑
            if (!ExternalDialogUtil.init) {
                ExternalDialogUtil.init()
            }

            processDialog(configBean)
        }


    }

    private fun isMeetTime(externalDialogAdConfigBean: ExternalDialogAdConfigBean): Boolean {

        val preference = PrivatePreference.getPreference()

        val periodAStartTime = getConfigTime(externalDialogAdConfigBean.periodAStart)
        val periodAFinishTime = getConfigTime(externalDialogAdConfigBean.periodAFinish)

        //时间判断
        val time = System.currentTimeMillis()
        if (time in periodAStartTime..periodAFinishTime) {
            val lastShowNoon = preference.getValue(PrefConst.KEY_EXTERNAL_DIALOG_LAST_SHOW_NOON, "")
            val format = SimpleDateFormat("MM/dd", Locale.CHINA)

            val currentDate = format.format(Date())
            if (currentDate != lastShowNoon) {
                preference.putValue(PrefConst.KEY_EXTERNAL_DIALOG_LAST_SHOW_NOON, currentDate).apply()
                statisticPeriod = 1
                return true
            }

            Logcat.d(ExternalDialogUtil.tag, "今天该时间段已展示过")
            return false
        }

        val periodBStartTime = getConfigTime(externalDialogAdConfigBean.periodBStart)
        val periodBFinishTime = getConfigTime(externalDialogAdConfigBean.periodBFinish)
        if (time in periodBStartTime..periodBFinishTime) {
            val lastShowNight = preference.getValue(PrefConst.KEY_EXTERNAL_DIALOG_LAST_SHOW_NIGHT, "")
            val format = SimpleDateFormat("MM/dd", Locale.CHINA)

            val currentDate = format.format(Date())
            if (currentDate != lastShowNight) {
                preference.putValue(PrefConst.KEY_EXTERNAL_DIALOG_LAST_SHOW_NIGHT, currentDate).apply()
                statisticPeriod = 2
                return true
            }

            Logcat.d(ExternalDialogUtil.tag, "今天该时间段已展示过")
            return false
        }

        Logcat.d(ExternalDialogUtil.tag, "不在时间段")

        return false
    }

    private fun getConfigTime(configTime: String): Long {
        val startLimitCalendar = Calendar.getInstance(Locale.CHINA)
        startLimitCalendar.time = Date()
        startLimitCalendar.set(Calendar.HOUR_OF_DAY, configTime.substring(0, 2).toInt())
        startLimitCalendar.set(Calendar.MINUTE, configTime.substring(2, 4).toInt())
        startLimitCalendar.set(Calendar.SECOND, 0)
        startLimitCalendar.set(Calendar.MILLISECOND, 0)
        return startLimitCalendar.time.time
    }


    private fun processDialog(configBean: ExternalDialogAdConfigBean) {
        //执行弹窗逻辑
        val satisfyDialogBean = ExternalDialogUtil.getSatisfyDialogBean(configBean)
        satisfyDialogBean?.let {
            val context = QuizAppState.getContext()
            val intent = ExternalDialogActivity.getIntent(context, it.id, statisticPeriod)
            context.startActivity(intent)
        }

    }

}