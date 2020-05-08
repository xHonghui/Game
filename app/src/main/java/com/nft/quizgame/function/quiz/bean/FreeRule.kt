package com.nft.quizgame.function.quiz.bean

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * 1	min_coin	int	否	最小金币
2	max_coin	int	否	最大金币
3	multiple_bonus_min_quiz	int	是	金币翻倍机会的最小题目
4	multiple_bonus_max_quiz	int	是	金币翻倍机会的最大题目
5	tips_card_interval	int	是	每隔多少题获取一次提示卡
 */
class FreeRule {

    @SerializedName("min_coin")
    var minCoin = 0
    @SerializedName("max_coin")
    var maxCoin = 0
    @SerializedName("multiple_bonus_min_quiz")
    var doubleBonusMinQuizCount = 0
    @SerializedName("multiple_bonus_max_quiz")
    var doubleBonusMaxQuizCount = 0
    @SerializedName("tips_card_interval")
    var tipsCardInterval = 0


    fun obtainRandomCoin(): Int {
        val random = Random()
        return minCoin + random.nextInt(maxCoin - minCoin + 1)
    }

    fun obtainCoinDesc(): String = "$minCoin~$maxCoin"
}