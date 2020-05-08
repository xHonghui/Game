package com.nft.quizgame.net

import com.nft.quizgame.R
import com.nft.quizgame.common.QuizAppState

abstract class RequestProperty {
    protected val isTestServer = QuizAppState.getContext().resources.getBoolean(R.bool.test_server)
    lateinit var host: String
    var apiKey: String = if (isTestServer) QuizAppState.getContext().getString(
            R.string.api_key_test) else QuizAppState.getContext().getString(R.string.api_key)
    var secretKey: String = if (isTestServer) QuizAppState.getContext().getString(
            R.string.secret_key_test) else QuizAppState.getContext().getString(R.string.secret_key)
    var desKey: String = if (isTestServer) QuizAppState.getContext().getString(
            R.string.des_key_test) else QuizAppState.getContext().getString(R.string.des_key)

    fun clear() {
        host = ""
        apiKey = ""
        secretKey = ""
        desKey = ""
    }
}