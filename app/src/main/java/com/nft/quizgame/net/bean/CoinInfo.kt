package com.nft.quizgame.net.bean

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
1	coin_code	string	否	金币类型
2	total_coin	int	否	历史总金币数
3	used_coin	int	否	使用金币数
4	existing_coin	int	否	现存金币数
 */
class CoinInfo {
    companion object {
        const val GOLD_COIN = "coin"
    }
    @SerializedName("coin_code")
    var coinCode: String? = null
    @SerializedName("total_coin")
    var totalCoin: Int = 0
    @SerializedName("used_coin")
    var usedCoin: Int = 0
    @SerializedName("existing_coin")
    var existingCoin: Int = 0

    @Expose(serialize = false, deserialize = false)
    var coinDiff:Int = 0
}