package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.function.quiz.bean.NewUserBonusRule

class RuleResponseBean : BaseResponseBean() {

    var data: RuleDTO? = null

    class RuleDTO {
        var rules: List<Rule>? = null
        @SerializedName("new_user_bonus")
        var newUserBonusRule: NewUserBonusRule? = null
    }
}