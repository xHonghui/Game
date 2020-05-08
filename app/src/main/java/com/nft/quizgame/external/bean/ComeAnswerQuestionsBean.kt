package com.nft.quizgame.external.bean

import com.nft.quizgame.R

class ComeAnswerQuestionsBean:BaseExternalDialogBean(3,4) {
    override fun getTitle(): String {
        return getString(R.string.come_answer_question)
    }

    override fun getContent(): CharSequence {

        return getString(R.string.come_answer_question_content)
    }

    override fun getBtnText(): String {
        return getString(R.string.goto_answer_question)
    }

    override fun specialConditions(): Boolean {
        return true
    }

    override fun timeInterval(): Long {
       return mHourTime * 24 * 7
    }

    override fun clickFrequencyClaim(): Int {
        return -1
    }

    override fun frequencyLimit(): Int {
        return 2
    }

}