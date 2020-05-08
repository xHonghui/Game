package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class CashOutRuleResponseBean : BaseResponseBean() {

    var data: CashOutRuleDTO? = null

    class CashOutRuleDTO {
        //withdraw_amounts	List<CashOutRuleConfig>	是	可用提现金额
        @SerializedName("withdraw_amounts")
        var withdrawAmounts: List<CashOutRuleBean>? = null

        //2	exchange_rate	int	是	（n）金币：(1)人民币,如：1000
       @SerializedName("exchange_rate")
        var exchangeRate:String? = null

        //3	is_first_time_withdraw	int	否	是否首次提现：0：否，1：是
        @SerializedName("is_first_time_withdraw")
        var isFirstTimeWithdraw:Int = 1
        //4	is_today_withdraw	int	否	今日是否提现过，0：否，1：是
        @SerializedName("is_today_withdraw")
        var isTodayWithdraw:Int = 0
    }

}