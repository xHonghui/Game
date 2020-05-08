package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class CashOutInfoResponseBean : BaseResponseBean() {

    var data: CashOutInfoDTO? = null

    class CashOutInfoDTO {
        @SerializedName("withdraw_info")
        var cashOutInfo: List<CashOutInfo>? = null
    }
}