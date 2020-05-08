package com.nft.quizgame.net.bean

import com.nft.quizgame.net.QuizRequestProperty

class RuleRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880502"
    }
    init {
        requestProperty = QuizRequestProperty()
    }
}