package com.nft.quizgame.function.quiz.bean.defaults

import com.nft.quizgame.function.quiz.bean.NewUserBonusRule
import com.nft.quizgame.function.quiz.bean.RuleCache
import com.nft.quizgame.net.bean.Rule

class DefaultNewUserBonusRule : Rule() {
    init {
        type = RuleCache.TYPE_NEW_USER_BONUS
        newUserBonusRule = NewUserBonusRule().apply {
            this.minBonus = 100
            this.maxBonus = 200
            this.realBonus = 1000
        }
    }
}