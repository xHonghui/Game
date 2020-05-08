package com.nft.quizgame.external.bean

import android.app.Activity
import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import kotlin.random.Random

class KnowledgeBean:BaseExternalDialogBean(11,9) {
    override fun getTitle(): String {
        return getString(R.string.knowledge_title)
    }

    override fun getContent(): CharSequence {
       return QuizAppState.getContext().getString(R.string.knowledge_content, Random.Default.nextInt(10500,89990))
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