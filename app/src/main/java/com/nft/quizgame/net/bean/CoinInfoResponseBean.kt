package com.nft.quizgame.net.bean

import com.google.gson.annotations.SerializedName

class CoinInfoResponseBean : BaseResponseBean() {

    var data: CoinInfoDTO? = null

    class CoinInfoDTO {
        @SerializedName("coins_info")
        var coinInfoList: List<CoinInfo>? = null
    }
}