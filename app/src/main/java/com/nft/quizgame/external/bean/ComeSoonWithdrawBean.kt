package com.nft.quizgame.external.bean

import android.app.Activity
import android.graphics.Color
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.function.user.UserViewModel

class ComeSoonWithdrawBean:BaseExternalDialogBean(4,5) {
    override fun getTitle(): String {
        return getString(R.string.come_soon_withdraw_title)
    }

    override fun getContent(): CharSequence {

        val existingCoin = AppViewModelProvider.getInstance().get(UserViewModel::class.java)
            .userData.value?.coinInfoData?.value?.existingCoin?:0


        val string = QuizAppState.getContext().getString(R.string.come_soon_withdraw_content, 20000 - existingCoin)

        return getDiffStr(string, Color.BLUE,getString(R.string.come_soon_withdraw_content_diff))
    }

    override fun getBtnText(): String {
        return getString(R.string.goto_answer_question)
    }

    override fun specialConditions(): Boolean {

        val value = AppViewModelProvider.getInstance().get(UserViewModel::class.java)
            .userData.value?.coinInfoData?.value
        val existingCoin =value ?.existingCoin?:0
        val totalCoin = value?.totalCoin ?: 0
        return existingCoin in 1001..19999 && totalCoin - existingCoin <= 0
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