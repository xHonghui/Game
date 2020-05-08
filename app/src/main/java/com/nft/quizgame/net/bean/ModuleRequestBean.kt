package com.nft.quizgame.net.bean

import com.nft.quizgame.net.QuizRequestProperty

class ModuleRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880500"
    }
    init {
        requestProperty = QuizRequestProperty()
    }
}