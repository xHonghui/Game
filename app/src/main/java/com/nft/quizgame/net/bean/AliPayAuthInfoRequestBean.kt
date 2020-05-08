package com.nft.quizgame.net.bean

import com.nft.quizgame.net.UserRequestProperty

class AliPayAuthInfoRequestBean :BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880111"
    }

    init {
        requestProperty = UserRequestProperty()
    }

    override fun needAccessToken(): Boolean {
        return false
    }

}