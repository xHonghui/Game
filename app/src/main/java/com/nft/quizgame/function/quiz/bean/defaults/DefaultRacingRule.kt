package com.nft.quizgame.function.quiz.bean.defaults

import com.nft.quizgame.function.quiz.bean.RacingRule
import com.nft.quizgame.function.quiz.bean.RuleCache
import com.nft.quizgame.net.bean.Rule

class DefaultRacingRule : Rule() {
    init {
        type = RuleCache.TYPE_RACING
        racingRule = RacingRule().apply {
            this.minCoin = 300
            this.maxCoin = 500
            this.limitTime = 120
            this.target = 25
        }
    }
}