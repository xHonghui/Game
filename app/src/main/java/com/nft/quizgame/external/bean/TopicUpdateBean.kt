package com.nft.quizgame.external.bean

import android.app.Activity
import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class TopicUpdateBean : BaseExternalDialogBean(5, 6) {
    override fun getTitle(): String {
        return getString(R.string.topic_update_title)
    }

    override fun getContent(): CharSequence {
        val format = SimpleDateFormat("MM/dd", Locale.CHINA)
        return QuizAppState.getContext().getString(R.string.topic_update_content, format.format(Date()), Random.Default.nextInt(500, 3950))
    }

    override fun getBtnText(): String {
       return getString(R.string.goto_answer_question)
    }

    override fun specialConditions(): Boolean {
        return true
    }

    override fun timeInterval(): Long {
        return 72 * mHourTime
    }

    override fun clickFrequencyClaim(): Int {
        return 3
    }

    override fun frequencyLimit(): Int {
       return -1
    }

}