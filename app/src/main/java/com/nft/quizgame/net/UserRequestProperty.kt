package com.nft.quizgame.net

import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState

class UserRequestProperty : RequestProperty() {
    init {
        host = if (isTestServer) QuizAppState.getContext().getString(
                R.string.user_host_test) else QuizAppState.getContext().getString(R.string.user_host)
    }
}