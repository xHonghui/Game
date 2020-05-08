package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.net.QuizRequestProperty

class WithdrawRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880509"
    }

    init {
        requestProperty = QuizRequestProperty()
    }

    //提现规则id
    @SerializedName("cash_out_id")
    var cashOutId: Int = 0
    //0：支付宝，1：微信
    val partner: Int = 0
    //用户真实姓名
    @SerializedName("user_real_name")
    var userRealName: String? = null
    //用户转账账号
    @SerializedName("withdrawal_account")
    var withdrawalAccount: String? = null


}