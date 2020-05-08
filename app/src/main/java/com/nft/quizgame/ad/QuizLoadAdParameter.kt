package com.nft.quizgame.ad

import android.content.Context
import com.nft.quizgame.common.ad.LoadAdParameter
import com.nft.quizgame.common.utils.AppUtils

class QuizLoadAdParameter(param1: Context, moduleId: Int) : LoadAdParameter(param1.applicationContext, moduleId) {
    init {
        this.adInterceptor = QuizAdInterceptor()

        this.virtualModuleIdConverter = if (AppUtils.isStorePkg(context)) {
            QuizVirtualModuleIdStoreConverter()
        } else {
            QuizVirtualModuleIdConverter()
        }
    }
}