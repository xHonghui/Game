package com.nft.quizgame.common.ad

interface AdLoadListener {
    fun onAdLoadSuccess(adBeanModuleId: Int)
    fun onAdLoadFail(statusCode: Int)
}