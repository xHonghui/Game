package com.nft.quizgame.external.bean

import android.graphics.Color
import com.nft.quizgame.R
import com.nft.quizgame.version.VersionController

class MillionSubsidiesBean:BaseExternalDialogBean(15,12) {
    override fun getTitle(): String {
        return getString(R.string.million_subsidies_title)
    }

    override fun getContent(): CharSequence {
        val string = getString(R.string.million_subsidies_content)
        val diff = getString(R.string.million_subsidies_content_diff)
        return getDiffStr(string, Color.BLUE,diff)
    }

    override fun getBtnText(): String {
        return getString(R.string.goto_answer_question)
    }
    override fun specialConditions(): Boolean {
        return VersionController.cdays >= 3
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