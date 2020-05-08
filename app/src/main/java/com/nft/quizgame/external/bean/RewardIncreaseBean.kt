package com.nft.quizgame.external.bean

import android.app.Activity
import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.version.VersionController
import java.text.SimpleDateFormat
import java.util.*

class RewardIncreaseBean:BaseExternalDialogBean(10,14) {
    override fun getTitle(): String {
        return getString(R.string.reward_increase_title)
    }

    override fun getContent(): CharSequence {
        val format = SimpleDateFormat("MM/dd", Locale.CHINA)
       return QuizAppState.getContext().getString(R.string.reward_increase_content,format.format(Date()))
    }

    override fun getBtnText(): String {
        return getString(R.string.goto_answer_question)
    }

    override fun specialConditions(): Boolean {

        return VersionController.cdays >= 5
    }

    override fun timeInterval(): Long {
     return -1
    }

    override fun clickFrequencyClaim(): Int {
        return -1
    }

    override fun frequencyLimit(): Int {
        return 1
    }

}