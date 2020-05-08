package com.nft.quizgame.net.bean

import com.nft.quizgame.net.QuizRequestProperty

class CashOutInfoRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880508"
    }

    init {
        requestProperty = QuizRequestProperty()
    }

    var size: Int = 0

}