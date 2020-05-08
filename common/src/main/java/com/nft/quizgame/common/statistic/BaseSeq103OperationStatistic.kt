package com.nft.quizgame.common.statistic

import android.text.TextUtils
import com.cs.bd.statistics.AbsBaseStatistic
import com.nft.quizgame.common.QuizAppState.getContext
import com.nft.quizgame.common.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 使用协议103的统计
 *
 * @author zouguiquan
 */
object BaseSeq103OperationStatistic : AbsBaseStatistic() {
    //操作统计-日志序列
    private const val OPERATION_LOG_SEQ = 103
    private val FUN_ID = getContext().resources.getInteger(R.integer.diff_config_statistic_103_fun_id)
    //广告统计
    private const val AD_REQUEST = "ad_request"
    private const val AD_FILLED = "ad_filled"
    private const val AD_SHOW = "ad_show"
    private const val AD_CLICK = "ad_click"
    private const val AD_CLOSE = "ad_close"
    private const val AD_END = "ad_end"

    const val HOME_ENTER = "home_enter"
    const val ENTRANCE_CLICK = "entrance_click"
    const val LUCKY_POCKET_SHOW = "luckypocket_show"
    const val RACING_MODE_ENTER = "speedmode_enter"
    const val FREE_MODE_ENTER = "freemode_enter"
    const val STAGE_MODE_ENTER = "levelmode_enter"
    const val OTHER_MODULE_ENTER = "othermodule_enter"
    const val MODE_GUIDE_SHOW = "modeguide_show"
    const val QUIZ_SHOW = "quiz_show"
    const val OPTION_CLICK = "option_click"
    const val TIPS_CARD_CLICK = "hintcard_click"
    const val TIPS_CARD_USE = "hintcard_use"
    const val EXCHANGE_CARD_CLICK = "exchangecard_click"
    const val EXCHANGE_CARD_USE = "exchangecard_use"
    const val DOUBLE_GUIDE_SHOW = "doubleguide_show"
    const val DOUBLE_GUIDE_CLICK = "doubleguide_click"
    const val DOUBLE_DONE = "double_done"
    const val ENVELOPE_BONUS_CLICK = "levelbonus_click"
    const val ENVELOPE_BONUS_POPUP_SHOW = "levelbonuspopup_show"
    const val ENVELOPE_BONUS_POPUP_CLICK = "levelbonuspopup_click"
    const val ENVELOPE_BONUS_OBTAIN = "levelbonus_obtain"
    const val CHALLENGE_FAILED = "challenge_failed"
    const val CHALLENGE_SUCCESS = "challenge_success"
    const val GAME_QUIT = "game_quit"
    const val VERSION_UPGRADE_ALERT = "upgraderemind_show"
    const val UPGRADE_BUTTON_CLICK = "upgrade_click"
    const val IGNORE_BUTTON_CLICK = "ignore_click"
    const val DOWNLOAD_PROGRESSBAR_SHOW = "progress_show"
    const val HIDE_BUTTON_CLICK = "hide_click"
    const val DOWNLOAD_APK_SUCCESS = "download_success"
    const val APK_INSTALL_REMIND = "installremind_show"
    const val INSTALL_BUTTON_CLICK = "install_click"
    const val QUIZ_DATA = "quiz_data"
    const val QUIZ_RUN_OUT = "quiz_runout"
    const val ANSWER_WRONG = "answer_wrong"
    const val OTHER_POPUP_SHOW = "otherpopup_show"

    /**
     * 广告请求
     */
    fun uploadAdRequest(moduleId: Int, entrance: String) {
        uploadData(obj = moduleId.toString(), optionCode = AD_REQUEST, entrance = entrance)
    }

    /**
     * 广告填充
     */
    fun uploadAdFilled(moduleId: Int, entrance: String) {
        uploadData(obj = moduleId.toString(), optionCode = AD_FILLED, entrance = entrance)
    }

    /**
     * 广告展示
     */
    fun uploadAdShow(moduleId: Int, entrance: String) {
        uploadData(obj = moduleId.toString(), optionCode = AD_SHOW, entrance = entrance)
    }

    /**
     * 广告点击
     */
    fun uploadAdClick(moduleId: Int, entrance: String) {
        uploadData(obj = moduleId.toString(), optionCode = AD_CLICK, entrance = entrance)
    }

    /**
     * 广告播放完毕
     */
    fun uploadAdEnd(moduleId: Int, entrance: String) {
        uploadData(obj = moduleId.toString(), optionCode = AD_END, entrance = entrance)
    }

    /**
     * 广告关闭
     */
    fun uploadAdClose(moduleId: Int, entrance: String) {
        uploadData(obj = moduleId.toString(), optionCode = AD_CLOSE, entrance = entrance)
    }


    /**
     * 上传操作统计数据
     *
     * @param context
     * @param funId      功能ID
     * @param obj     统计对象
     * @param optionCode 操作代码
     */
    fun uploadData(funId: Int = FUN_ID, obj: String? = "", optionCode: String? = "", optionResults: Int = OPERATE_SUCCESS, entrance: String? = "", tabCategory: String? = "", position: String? = "",
                   associatedObj: String? = "", aId: String? = "", remark: String? = "") {
        if (TextUtils.isEmpty(optionCode)) {
            throw IllegalArgumentException("optionCode cannot be empty")
        }
        GlobalScope.launch {
            val buffer = StringBuffer()
            //功能点ID
            buffer.append(funId)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //统计对象(mapId)
            buffer.append(obj)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //操作代码
            buffer.append(optionCode)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //操作结果-----0:未成功,1:成功(默认成功)
            buffer.append(optionResults)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //入口(本次需求为空)
            buffer.append(entrance)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //Tab分类(本次需求为空)
            buffer.append(tabCategory)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //位置--统计对象(AppID)的所在位置.-(本次需求为空)
            buffer.append(position)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //关联对象(是否传值以特定的"操作代码"为准)
            buffer.append(associatedObj)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //广告ID(是否传值以特定的”操作代码“为准)
            buffer.append(aId)
            buffer.append(STATISTICS_DATA_SEPARATE_STRING)
            //备注(是否传值以特定的”操作代码“为准)
            buffer.append(remark)
            //上传统计数据
            uploadStatisticData(getContext(), OPERATION_LOG_SEQ, funId, buffer)
        }
    }
}