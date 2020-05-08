package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName
import com.nft.quizgame.net.CoinRequestProperty


/**
1	opt_type	int	否	操作类型，0：充值，1：消费，2：提现
2	coin_code	string	否	操作金币的金币类型
3	opt_coin	int	否	操作金币数目
4	description	string	否	操作说明
 */
class CoinOptRequestBean : BaseRequestBean() {
    companion object {
        const val REQUEST_PATH = "/ISO1880203"
    }

    init {
        requestProperty = CoinRequestProperty()
    }

    @SerializedName("tran_id")
    var orderId: String? = null
}