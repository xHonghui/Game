package com.nft.quizgame.net

import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState

class QuizRequestProperty : RequestProperty() {
    init {
        host = if (isTestServer) QuizAppState.getContext().getString(
                R.string.quiz_host_test) else QuizAppState.getContext().getString(R.string.quiz_host)
    }
}