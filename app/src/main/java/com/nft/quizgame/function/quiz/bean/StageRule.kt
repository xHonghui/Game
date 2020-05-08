package com.nft.quizgame.function.quiz.bean

import com.google.gson.annotations.SerializedName
import java.util.*

/**
1	min_coin	int	否	最小金币
2	max_coin	int	否	最大金币
3	multiple_bonus_min_quiz	int	是	金币翻倍机会的最小题目
4	multiple_bonus_max_quiz	int	是	金币翻倍机会的最大题目
6	tips_card_interval	int	是	每隔多少题获取一次提示卡
7	switch_card_interval	int	是	每隔多少题获取一次换题卡
8	extra_bonus_interval	int	是	额外奖励间隔
9	extra_bonus_coin	int	是	额外奖励金币数
10	red_package_interval	int	是	每隔多少题可出现红包奖励
11	red_package_bonus	int	是	红包奖励数
 */
class StageRule {

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
    @SerializedName("switch_card_interval")
    var changeCardInterval = 0
    @SerializedName("red_package_interval")
    var envelopeInterval = 0
    @SerializedName("red_package_bonus")
    var envelopeCoin = 0

    fun obtainRandomCoin(): Int {
        val random = Random()
        return minCoin + random.nextInt(maxCoin - minCoin + 1)
    }

    fun obtainCoinDesc(): String = "$minCoin~$maxCoin"
}