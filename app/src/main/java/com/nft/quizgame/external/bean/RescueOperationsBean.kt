package com.nft.quizgame.external.bean

import android.app.Activity
import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import kotlin.random.Random

class RescueOperationsBean:BaseExternalDialogBean(13,15) {
    override fun getTitle(): String {
        return getString(R.string.rescue_operations_title)
    }

    override fun getContent(): CharSequence {
        return QuizAppState.getContext().getString(R.string.rescue_operations_content, Random.Default.nextInt(11,29))
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
}