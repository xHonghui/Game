package com.nft.quizgame.function.quiz.bean

import com.google.gson.annotations.SerializedName
import java.util.*

/**
1	min_coin	int	是	最小金币
2	max_coin	int	是	最大金币
3	limit_time	int	是	限时（m）
4	target_number	int	是	目标答对数
 */
class RacingRule {
    @SerializedName("min_coin")
    var minCoin = 0
    @SerializedName("max_coin")
    var maxCoin = 0
    @SerializedName("limit_time")
    var limitTime = 0
    @SerializedName("target_number")
    var target = 0

    fun obtainRandomCoin(): Int {
        val random = Random()
        return minCoin + random.nextInt(maxCoin - minCoin + 1)
    }

    fun obtainCoinDesc(): String = "$minCoin~$maxCoin"
}