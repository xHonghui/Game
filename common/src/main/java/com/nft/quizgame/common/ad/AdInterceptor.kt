package com.nft.quizgame.common.ad

interface AdInterceptor {
    fun isLoadAd(adModuleId: Int, timeGapLimit: Boolean): Boolean
}