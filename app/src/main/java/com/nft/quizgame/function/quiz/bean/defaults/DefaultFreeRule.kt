package com.nft.quizgame.function.quiz.bean.defaults

import com.nft.quizgame.function.quiz.bean.FreeRule
import com.nft.quizgame.function.quiz.bean.RuleCache
import com.nft.quizgame.net.bean.Rule

class DefaultFreeRule : Rule() {
    init {
        type = RuleCache.TYPE_FREE
        freeRule = FreeRule().apply {
            this.minCoin = 10
            this.maxCoin = 50
            this.doubleBonusMinQuizCount = 2
            this.doubleBonusMaxQuizCount = 6
            this.tipsCardInterval = 5
        }
    }
}