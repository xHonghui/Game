package com.nft.quizgame.common.ad

import android.content.Context
import com.cs.bd.ad.AdSdkApi
import com.cs.bd.ad.http.bean.BaseModuleDataItemBean
import com.cs.bd.ad.manager.AdSdkManager
import com.cs.bd.ad.sdk.bean.SdkAdSourceAdWrapper


open class AdData(internal val adObj: Any, val adSource: Int, val adStyle: Int,
                  val baseModuleDataItemBean: BaseModuleDataItemBean,
                  val sdkAdSourceAdWrapper: SdkAdSourceAdWrapper,
                  val adListener: AdSdkManager.IVLoadAdvertDataListener) {

    private var startShowTime: Long = 0

    fun uploadClick(context: Context) {
        AdSdkApi.sdkAdClickStatistic(context, baseModuleDataItemBean,
                sdkAdSourceAdWrapper, "")
    }

    fun uploadShow(context: Context) {
        AdSdkApi.sdkAdShowStatistic(context, baseModuleDataItemBean,
                sdkAdSourceAdWrapper, "")
    }

}
