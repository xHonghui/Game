package com.nft.quizgame.common.ad

import android.app.Activity
import android.view.ViewGroup
import com.cs.bd.ad.http.bean.BaseModuleDataItemBean
import com.cs.bd.ad.manager.AdSdkManager
import com.cs.bd.ad.sdk.bean.SdkAdSourceAdWrapper
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.ads.rewardvideo.RewardVideoAD
import com.qq.e.ads.splash.SplashAD

class GDTAdData(adObj: Any, adSource: Int, adStyle: Int, baseModuleDataItemBean: BaseModuleDataItemBean,
                sdkAdSourceAdWrapper: SdkAdSourceAdWrapper, adListener: AdSdkManager.IVLoadAdvertDataListener) :
        AdData(adObj, adSource, adStyle, baseModuleDataItemBean, sdkAdSourceAdWrapper, adListener) {

    fun getNativeExpressAds(): MutableList<NativeExpressADView> {
        return if (adObj is MutableList<*>) {
            adObj.forEach {
                check(it is NativeExpressADView) { "adObj must be MutableList<NativeExpressADView>" }
            }
            adObj as MutableList<NativeExpressADView>
        } else {
            check(adObj is NativeExpressADView) { "adObj must be NativeExpressADView" }
            val arrayList = ArrayList<NativeExpressADView>(1)
            arrayList.add(adObj)
            arrayList

        }
    }

    fun showInterstitialAd(activity: Activity?) {
        check(adObj is UnifiedInterstitialAD)
        if (activity == null) {
            adObj.show()
        } else {
            adObj.show(activity)
        }
    }

    fun showInterstitialAdAsPopupWindow(activity: Activity?) {
        check(adObj is UnifiedInterstitialAD)
        if (activity == null) {
            adObj.showAsPopupWindow()
        } else {
            adObj.showAsPopupWindow(activity)
        }
    }

    fun fetchSplashAd(container: ViewGroup) {
        check(adObj is SplashAD)
        adObj.fetchAndShowIn(container)
    }

    fun showRewardVideo(activity: Activity) {
        check(adObj is RewardVideoAD)
        adObj.showAD()
    }
}