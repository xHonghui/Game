package com.nft.quizgame.external.bean

import android.graphics.Color
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.MainActivity
import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.function.quiz.QuizPropertyViewModel
import com.nft.quizgame.function.quiz.bean.RacingRule
import com.nft.quizgame.function.quiz.bean.defaults.DefaultModuleConfig
import com.nft.quizgame.function.sync.GlobalPropertyViewModel
import com.nft.quizgame.function.sync.bean.GlobalPropertyBean

class ChallengeUndoneBean : BaseExternalDialogBean(1, 2) {


    override fun getTitle(): String {
        return getString(R.string.challenge_undone_title)
    }

    override fun getContent(): CharSequence {
        //金币数取当前竞速模式配置

        val racingRule: RacingRule? = if (dataIsLoadSuccess()) {
            AppViewModelProvider.getInstance().get(QuizPropertyViewModel::class.java)
                    .getRule(DefaultModuleConfig.MODULE_CODE_3).racingRule
        } else {
            null
        }
        var minCoin = 0
        var maxCoin = 0

        racingRule?.let {
            minCoin = it.minCoin
            maxCoin = it.maxCoin
        }
        val context = QuizAppState.getContext()
        val string = context
                .getString(R.string.challenge_undone_content, minCoin, maxCoin)
        val diffStr = context.getString(R.string.challenge_undone_content_diff)
        return getDiffStr(string, Color.BLUE, diffStr)

    }

    override fun getBtnText(): String {
        return getString(R.string.goto_challenge)
    }

    override fun specialConditions(): Boolean {
        //今天未挑战
        return AppViewModelProvider.getInstance().get(GlobalPropertyViewModel::class.java).getChallengeState() != GlobalPropertyBean.CHALLENGE_STATE_SUCCESS
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
        return MainActivity.ENTER_FUNCTION_CHALLENGE
    }

}