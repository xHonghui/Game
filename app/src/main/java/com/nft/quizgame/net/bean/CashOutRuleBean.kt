package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class CashOutRuleBean {

    /**
     * 1	cash_out_id	int	否	提现规则id
    2	real_currency	decimal	否	可提现人民币
    3	coin_amount	int	否 对应金币数量
    4	limit_per_day	int	否	每日限制份额
     */

    @SerializedName("cash_out_id")
    var cashOutId: Int = 0
    @SerializedName("real_currency")
    var realCurrency: String? = null
    @SerializedName("coin_amount")
    var coinAmount: Int = 0
    @SerializedName("limit_per_day")
    var limitPerDay: Int = 0

}