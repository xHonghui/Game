package com.nft.quizgame.net.bean

import com.nft.quizgame.net.UserRequestProperty

class RefreshTokenRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880106"
    }

    init {
        requestProperty = UserRequestProperty()
    }

    override fun needAccessToken(): Boolean {
        return false
    }


}