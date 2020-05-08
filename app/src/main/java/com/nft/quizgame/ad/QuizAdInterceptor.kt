package com.nft.quizgame.ad

import com.nft.quizgame.common.ad.AdInterceptor

class QuizAdInterceptor : AdInterceptor {

    override fun isLoadAd(adModuleId: Int, timeGapLimit: Boolean): Boolean {
        return true
    }
}