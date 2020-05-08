package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class Token {

    /**
     * 1    access_token    string  否   访问token
    2	expired_in	int	否	以秒表示的access_token的有效期
    3   refresh_token   string  否   刷新token
     */

    @SerializedName("access_token")
    var accessToken: String? = null
    @SerializedName("expired_in")
    var expiredIn: Int = 0
    @SerializedName("refresh_token")
    var refreshToken: String? = null
}