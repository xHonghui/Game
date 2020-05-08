package com.nft.quizgame.net

import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState

class CoinRequestProperty : RequestProperty() {
    init {
        host = if (isTestServer) QuizAppState.getContext().getString(
                R.string.coin_host_test) else QuizAppState.getContext().getString(R.string.coin_host)
    }
}