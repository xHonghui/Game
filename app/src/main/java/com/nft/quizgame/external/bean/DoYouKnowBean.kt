package com.nft.quizgame.external.bean

import com.nft.quizgame.MainActivity
import com.nft.quizgame.R

class DoYouKnowBean : BaseExternalDialogBean(12, 10) {
    override fun getTitle(): String {
        return getString(R.string.do_you_know_title)
    }

    override fun getContent(): CharSequence {
        return getString(R.string.do_you_know_content)
    }

    override fun getBtnText(): String {
        return getString(R.string.go_and_see)
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

    override fun enter(): String? {
        return MainActivity.ENTER_FUNCTION_STRONGEST_BRAIN
    }
}