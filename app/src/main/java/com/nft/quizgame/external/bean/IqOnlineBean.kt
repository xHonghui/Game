package com.nft.quizgame.external.bean

import android.app.Activity
import com.nft.quizgame.R

class IqOnlineBean : BaseExternalDialogBean(9, 7) {
    override fun getTitle(): String {
        return getString(R.string.iq_online_title)
    }

    override fun getContent(): CharSequence {
        return getString(R.string.iq_online_content)
    }

    override fun getBtnText(): String {
        return getString(R.string.goto_answer_question)
    }

    override fun specialConditions(): Boolean {
        return true
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