package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class CashOutInfo {
    @SerializedName("username")
    var userName: String? = null
    @SerializedName("withdraw_amount")
    var amount: Int = 0
}