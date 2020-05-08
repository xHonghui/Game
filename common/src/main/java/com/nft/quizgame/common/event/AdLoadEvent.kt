package com.nft.quizgame.common.event

sealed class AdLoadEvent(val adBeanModuleId: Int, val loadFailCode: Int?) {
    class OnAdLoadSuccess(adBeanModuleId: Int) : AdLoadEvent(adBeanModuleId, null)
    class OnAdLoadFail(adBeanModuleId: Int,loadFailCode: Int):AdLoadEvent(adBeanModuleId,loadFailCode)
}