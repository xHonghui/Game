package com.nft.quizgame.external.bean

import android.graphics.Color
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.MainActivity
import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.function.sync.GlobalPropertyViewModel

class ChallengeInviteBean : BaseExternalDialogBean(2, 3) {

    override fun getTitle(): String {
        return getString(R.string.get_through_invite)
    }

    override fun getContent(): CharSequence {
        val currentStage =
            AppViewModelProvider.getInstance().get(GlobalPropertyViewModel::class.java)
                .getCurrentStage()

        val string = QuizAppState.getContext().getString(R.string.get_through_invite_content, currentStage)
        val diffOne = getString(R.string.get_through_invite_content_diff_one)
        val diffTwo = getString(R.string.get_through_invite_content_diff_two)


        return getDiffStr(string, Color.BLUE, diffOne, diffTwo)
    }

    override fun getBtnText(): String {
        return getString(R.string.goto_get_through)
    }

    override fun specialConditions(): Boolean {
        //当前闯关数≥3
        val currentStage =
            AppViewModelProvider.getInstance().get(GlobalPropertyViewModel::class.java)
                .getCurrentStage()
        return currentStage >= 3
    }

    override fun timeInterval(): Long {
        return 48L * mHourTime
    }

    override fun clickFrequencyClaim(): Int {
        return 3
    }


    override fun frequencyLimit(): Int {
        return -1
    }

    override fun enter(): String? {
        return MainActivity.ENTER_FUNCTION_STAGE
    }
}