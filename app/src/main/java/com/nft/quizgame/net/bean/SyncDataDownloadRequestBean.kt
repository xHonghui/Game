package com.nft.quizgame.net.bean

import com.nft.quizgame.net.QuizRequestProperty

class SyncDataDownloadRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880511"
    }
    init {
        requestProperty = QuizRequestProperty()
    }
}