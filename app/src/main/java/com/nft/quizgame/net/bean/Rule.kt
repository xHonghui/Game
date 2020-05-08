package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.function.quiz.bean.FreeRule
import com.nft.quizgame.function.quiz.bean.NewUserBonusRule
import com.nft.quizgame.function.quiz.bean.RacingRule
import com.nft.quizgame.function.quiz.bean.StageRule

open class Rule {
    @SerializedName("module_code")
    var moduleCode: Int? = null
    @SerializedName("rule_code")
    var type: Int = -1
    @SerializedName("free_mode_rule")
    var freeRule: FreeRule? = null
    @SerializedName("break_mode_rule")
    var stageRule: StageRule? = null
    @SerializedName("race_mode_rule")
    var racingRule: RacingRule? = null
    @SerializedName("new_user_bonus")
    var newUserBonusRule: NewUserBonusRule? = null
}