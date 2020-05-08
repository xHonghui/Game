package com.nft.quizgame.net.bean

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.net.RequestProperty

abstract class BaseRequestBean {
    @Expose(serialize = false, deserialize = false)
    lateinit var requestProperty: RequestProperty
    @Expose(serialize = false, deserialize = false)
    var accessToken: String = QuizAppState.getFacade().getUserAccessToken() ?: ""

    @SerializedName("refresh_token")
    var refreshToken: String? = QuizAppState.getFacade().getUserRefreshToken()

    var device: Device = Device()

    fun resetAccessToken() {
        accessToken = QuizAppState.getFacade().getUserAccessToken() ?: ""
    }


    open fun needAccessToken(): Boolean {
        return true
    }
}