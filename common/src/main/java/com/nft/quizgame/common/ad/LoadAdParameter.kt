package com.nft.quizgame.common.ad

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.common.event.AdLoadEvent
import com.nft.quizgame.common.event.Event

abstract class LoadAdParameter(val context: Context, val moduleId: Int) {
    var adCount: Int = 1
    var feedViewWidth: Int = 0
    var timeGapLimit: Boolean = true
    var splashContainer: ViewGroup? = null
    var adInterceptor: AdInterceptor? = null
    var virtualModuleIdConverter: VirtualModuleIdConverter? = null

    //1自由 2闯关 3竞速 统计使用
    var entrance = ""
}