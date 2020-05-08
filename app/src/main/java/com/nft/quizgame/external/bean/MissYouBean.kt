package com.nft.quizgame.external.bean

import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.pref.PrefConst
import com.nft.quizgame.common.pref.PrivatePreference

class MissYouBean : BaseExternalDialogBean(6, 13) {
    override fun getTitle(): String {
        return getString(R.string.miss_you_title)
    }

    override fun getContent(): CharSequence {
        val currentTimeMillis = System.currentTimeMillis()

        val interval = currentTimeMillis - PrivatePreference.getPreference().getValue(
            PrefConst.KEY_MAIN_LAST_SHOW_TIME,
            currentTimeMillis
        )

        var day = 0

        if (interval > 0) {
            day = (interval / (24 * mHourTime) + 1).toInt()
        }

        return QuizAppState.getContext().getString(R.string.miss_you_content, day)
    }

    override fun getBtnText(): String {
        return getString(R.string.go_and_see)
    }

    override fun specialConditions(): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        val interval = currentTimeMillis - PrivatePreference.getPreference().getValue(
            PrefConst.KEY_MAIN_LAST_SHOW_TIME,
            currentTimeMillis
        )

        return interval > 24 * mHourTime
    }

    override fun timeInterval(): Long {
        return 48 * mHourTime
    }

    override fun clickFrequencyClaim(): Int {
        return -1
    }

    override fun frequencyLimit(): Int {
        return 2
    }

}