package com.nft.quizgame.function.quiz.bean.defaults

import com.nft.quizgame.function.quiz.bean.RuleCache
import com.nft.quizgame.function.quiz.bean.StageRule
import com.nft.quizgame.net.bean.Rule

class DefaultStageRule : Rule() {
    init {
        type = RuleCache.TYPE_STAGE
        stageRule = StageRule().apply {
            this.minCoin = 10
            this.maxCoin = 50
            this.doubleBonusMinQuizCount = 2
            this.doubleBonusMaxQuizCount = 6
            this.tipsCardInterval = 5
            this.changeCardInterval = 10
            this.envelopeInterval = 20
            this.envelopeCoin = 300
        }
    }
}