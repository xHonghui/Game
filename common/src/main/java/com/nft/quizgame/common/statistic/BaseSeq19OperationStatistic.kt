package com.nft.quizgame.common.statistic

import com.cs.statistic.StatisticsManager
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.QuizAppState.getContext
import com.nft.quizgame.common.QuizEnv
import com.nft.quizgame.common.R
import com.nft.quizgame.common.utils.Logcat

/**
 * @author kingyang
 */
object BaseSeq19OperationStatistic {

    private var sHasUploadNew = false

    fun uploadBasicInfo() {
        var isNew = false
        if (QuizAppState.getFacade().isFirstRun() && !sHasUploadNew) {
            isNew = true
            sHasUploadNew = true
        }
        Logcat.i("Test", "uploadBasicInfo")
        StatisticsManager.getInstance(getContext()).upLoadBasicInfoStaticData(
                getContext().resources.getInteger(R.integer.diff_config_statistic_19_product_id).toString(),
                QuizEnv.sChannelId, false, false, "", isNew, null)
    }
}