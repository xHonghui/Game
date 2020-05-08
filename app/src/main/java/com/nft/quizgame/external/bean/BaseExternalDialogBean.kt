package com.nft.quizgame.external.bean

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.MainActivity
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.State
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.config.bean.ExternalDialogAdConfigBean
import com.nft.quizgame.data.AppDatabase
import com.nft.quizgame.external.ExternalDialogUtil
import com.nft.quizgame.function.user.UserViewModel
import com.nft.quizgame.statistic.Statistic103
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * 必须在子进程创建，需要从数据库中加载数据
 */
abstract class BaseExternalDialogBean(
        val id: Int,
        val priority: Int
) : Serializable {

    var showCount: Int = 0
    var lastShowTime: Long = 0
    var clickData: Int = 0
    val mHourTime = 60 * 60 * 1000L

    var statisticPeriod = 0

    init {
        val queryDialogBean = AppDatabase.getInstance().externalDialogDao().queryDialogBean(id)
        queryDialogBean?.let {
            showCount = it.showCount
            lastShowTime = it.lastShowTime
            clickData = it.clickData
        }
    }


    fun satisfy(configBean: ExternalDialogAdConfigBean): Boolean {

        val list = configBean.list
        if (list.size >= id && !list.get(id - 1)) {
            //配置不打开
            Logcat.d(ExternalDialogUtil.tag, "id $id 配置关闭")
            return false
        }

        val b = specialConditions() &&
                System.currentTimeMillis() - lastShowTime > timeInterval() &&
                clickClaim() &&
                (frequencyLimit() < 0 || showCount < frequencyLimit())

        Logcat.d(ExternalDialogUtil.tag, toString())
        Logcat.d(ExternalDialogUtil.tag, "${javaClass.name} satisfy = $b")
        return b
    }


    fun onShow() {
        Statistic103.uploadExternalpopupShow(id, statisticPeriod)

        showCount += 1

        resetOnClick()

        lastShowTime = System.currentTimeMillis()


        saveData()
    }

    private fun onClick() {
        Statistic103.uploadExternalpopupClick(id, statisticPeriod)
        val clickFrequencyClaim = clickFrequencyClaim()
        if (clickFrequencyClaim > 0) {
            val i = (showCount - 1) % clickFrequencyClaim

            if (!bitIsSet(clickData, i)) {
                clickData = setIntBit(clickData, i)
            }
        }

        saveData()
    }


    abstract fun getTitle(): String
    abstract fun getContent(): CharSequence
    abstract fun getBtnText(): String

    abstract fun specialConditions(): Boolean

    abstract fun timeInterval(): Long

    //点击次数要求，返回几次内需要点击，>0表示需要该规则
    abstract fun clickFrequencyClaim(): Int

    abstract fun frequencyLimit(): Int

    open fun enter(): String? {
        return null
    }

    open fun jump(activity: Activity) {
        onClick()
        val startIntent = Intent()

        /* val startIntent = Intent(Intent.ACTION_MAIN)
         startIntent.addCategory(Intent.CATEGORY_LAUNCHER)*/
        startIntent.component = (ComponentName(activity, MainActivity::class.java))
        startIntent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startIntent.putExtra(MainActivity.KEY_ENTER_FUNCTION, enter())
        startIntent.putExtra(MainActivity.KEY_ENTER, MainActivity.ENTER_EXTERNAL_DIALOG)
        activity.startActivity(startIntent)
    }


    open fun clickClaim(): Boolean {

        val clickFrequencyClaim = clickFrequencyClaim()

        if (clickFrequencyClaim > 0 && showCount >= clickFrequencyClaim) {

            for (i in 0 until clickFrequencyClaim) {
                if (bitIsSet(clickData, i)) {
                    return true
                }
            }

            return false

        }

        return true

    }


    private fun resetOnClick() {

        val clickFrequencyClaim = clickFrequencyClaim()
        if (clickFrequencyClaim > 0) {
            val i = (showCount - 1) % clickFrequencyClaim
            if (bitIsSet(clickData, i)) {
                clickData = resetIntBit(clickData, i)
            }
        }
    }


    fun bitIsSet(data: Int, position: Int): Boolean {
        return data and (1 shl position) > 0
    }


    fun resetIntBit(data: Int, position: Int): Int {
        return data and (1 shl position).inv()
    }

    fun setIntBit(data: Int, position: Int): Int {
        return data or (1 shl position)
    }

    override fun toString(): String {
        return "BaseExternalDialogBean(id=$id, title='${getTitle()}', priority=$priority, showCount=$showCount, lastShowTime=$lastShowTime, clickData=$clickData)"
    }


    fun getString(strRes: Int): String {
        val context = QuizAppState.getContext()
        return context.getString(strRes)
    }


    fun getDiffStr(content: String, diffColor: Int, vararg diffs: String): SpannableString {

        val spannableString = SpannableString(content)

        for (diff in diffs) {
            val indexOf = content.indexOf(diff)
            spannableString.setSpan(
                    ForegroundColorSpan(Color.parseColor("#35A2FF")),
                    indexOf,
                    indexOf + diff.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }


        return spannableString
    }

    fun saveData() {
        GlobalScope.launch(Dispatchers.IO) {
            val bean = ExternalDialogBean()
            bean.id = id
            bean.showCount = showCount
            bean.clickData = clickData
            bean.lastShowTime = lastShowTime

            AppDatabase.getInstance().externalDialogDao().saveDialogBean(bean)
        }

    }

    fun dataIsLoadSuccess(): Boolean {
        return AppViewModelProvider.getInstance().get(UserViewModel::class.java).initAppDataState.value?.peekContent() is State.Success
    }

}