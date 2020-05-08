package com.nft.quizgame.function.quiz.bean

import com.google.gson.annotations.SerializedName
import java.util.*

/**
1	min_bonus	int	是	最小奖励金币数
2	max_bonus	int	是	最大奖励金币数
3	real_bonus	int	否	实际用户获取的红包金币数
 */
class NewUserBonusRule {
    @SerializedName("min_bonus")
    var minBonus = 0
    @SerializedName("max_bonus")
    var maxBonus = 0
    @SerializedName("real_bonus")
    var realBonus = 0

    fun obtainRandomCoin(): Int {
        val random = Random()
        return minBonus + random.nextInt(maxBonus - minBonus + 1)
    }

    fun obtainCoinDesc(): String = "$minBonus~$maxBonus"
}