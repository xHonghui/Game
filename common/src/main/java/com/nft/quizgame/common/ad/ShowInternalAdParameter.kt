package com.nft.quizgame.common.ad

import android.app.Activity
import android.view.ViewGroup

class ShowInternalAdParameter(var activity: Activity?, var adBean: AdBean, var container: ViewGroup? = null) {
    var dilutionViewGroup: ViewGroup? = null
//    var listener: AdBean.AdInteractionListener? = null
    var slideIntervalTimeBanner: Int = 0
}