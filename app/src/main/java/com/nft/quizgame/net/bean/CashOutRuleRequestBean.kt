package com.nft.quizgame.net.bean

import com.nft.quizgame.net.QuizRequestProperty

//提现可选金额获取
class CashOutRuleRequestBean :BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880507"
    }

    init {
        requestProperty = QuizRequestProperty()
    }

}