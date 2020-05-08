package com.nft.quizgame.common.ad

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.sdk.openadsdk.*
import com.cs.bd.ad.http.bean.BaseModuleDataItemBean
import com.cs.bd.ad.manager.AdSdkManager
import com.cs.bd.ad.sdk.bean.SdkAdSourceAdWrapper


class TTAdData(adObj: Any, adSource: Int, adStyle: Int, baseModuleDataItemBean: BaseModuleDataItemBean,
               sdkAdSourceAdWrapper: SdkAdSourceAdWrapper, adListener: AdSdkManager.IVLoadAdvertDataListener, val views: List<View>?) :
        AdData(adObj, adSource, adStyle, baseModuleDataItemBean, sdkAdSourceAdWrapper, adListener) {


    fun getAdCount(): Int {
        if (adObj is MutableList<*>) {
            return adObj.count()
        }

        return 1
    }

    fun getNativeAd(adIndex: Int): TTNativeAd? {
        if (adObj is TTNativeAd) {
            if (adIndex == 0) {
                return adObj
            }
        } else if (adObj is MutableList<*> && adIndex >= 0 && adIndex < adObj.count()) {
            if (adObj[adIndex] is TTNativeAd) {
                return adObj[adIndex] as TTNativeAd
            }
        }
        return null
    }

    fun bindNativeAd(nativeAd: TTNativeAd, nativeAdContainer: ViewGroup, titleId: Int = 0,
                     descId: Int = 0, dislikeId: Int = 0, logoId: Int = 0, videoContainerId: Int = 0,
                     setupClickView: ((clickViewList: ArrayList<View>) -> Unit)?,
                     setupCreativeView: ((creativeViewList: ArrayList<View>) -> Unit)?,
                     setupIconView: ((image: TTImage) -> Unit)?,
                     setupImageView: ((index: Int, image: TTImage) -> Unit)?) {
        var titleView: TextView? = null
        if (titleId > 0) {
            titleView = nativeAdContainer.findViewById(titleId) as TextView
        }
        var descView: TextView? = null
        if (descId > 0) {
            descView = nativeAdContainer.findViewById(descId) as TextView
        }
        var dislikeView: ImageView? = null
        if (dislikeId > 0) {
            dislikeView = nativeAdContainer.findViewById(dislikeId) as ImageView
        }
        var logoView: ImageView? = null
        if (logoId > 0) {
            logoView = nativeAdContainer.findViewById(logoId) as ImageView
        }
        var videoContainer: ViewGroup? = null
        if (videoContainerId > 0) {
            videoContainer = nativeAdContainer.findViewById(videoContainerId) as ViewGroup
        }
        bindNativeAd(nativeAd, nativeAdContainer, titleView, descView, dislikeView, logoView, videoContainer,
                setupClickView, setupCreativeView, setupIconView, setupImageView)
    }

    fun bindNativeAd(nativeAd: TTNativeAd, nativeAdContainer: ViewGroup, titleView: TextView? = null,
                     descView: TextView? = null, dislikeView: ImageView? = null, logoView: ImageView? = null,
                     videoContainer: ViewGroup? = null,
                     setupClickView: ((clickViewList: ArrayList<View>) -> Unit)?,
                     setupCreativeView: ((creativeViewList: ArrayList<View>) -> Unit)?,
                     setupIconView: ((image: TTImage) -> Unit)?,
                     setupImageView: ((index: Int, image: TTImage) -> Unit)?) {

        //可以被点击的view, 也可以把nativeView放进来意味整个广告区域可被点击
        val clickViewList = ArrayList<View>()
        setupClickView?.invoke(clickViewList)
        //触发创意广告的view（点击下载或拨打电话）
        val creativeViewList = ArrayList<View>()
        setupCreativeView?.invoke(creativeViewList)

        logoView?.setImageBitmap(nativeAd.adLogo)
        titleView?.text = nativeAd.title
        descView?.text = nativeAd.description
        if (nativeAd.imageMode == TTAdConstant.IMAGE_MODE_VIDEO) {
            videoContainer?.addView(nativeAd.adView)
            videoContainer?.visibility = View.VISIBLE
        } else {
            if (nativeAd.imageList != null && nativeAd.imageList.isNotEmpty()) {
                nativeAd.imageList.forEachIndexed { index, image ->
                    setupImageView?.invoke(index, image)
                }
            }
        }
        setupIconView?.invoke(nativeAd.icon)
//        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
        nativeAd.registerViewForInteraction(nativeAdContainer, clickViewList,
                creativeViewList, dislikeView, object : TTNativeAd.AdInteractionListener {
            override fun onAdClicked(view: View, ad: TTNativeAd?) {
                adListener.onAdClicked(null)
            }

            override fun onAdCreativeClick(view: View, ad: TTNativeAd?) {
                adListener.onAdClicked(null)
            }

            override fun onAdShow(ad: TTNativeAd?) {
                adListener.onAdShowed(null)
            }
        })
    }

    fun getBannerAd(slideIntervalTime: Int = 0,
                    downloadListener: TTAppDownloadListener? = null,
                    dislikeCallback: TTAdDislike.DislikeInteractionCallback? = null): View {
        check(adObj is TTBannerAd) { "adObj is not TTBannerAd" }
        if (slideIntervalTime > 0) {
            adObj.setSlideIntervalTime(slideIntervalTime)
        }
        adObj.setBannerInteractionListener(object : TTBannerAd.AdInteractionListener {
            override fun onAdClicked(p0: View?, p1: Int) {
                adListener.onAdClicked(null)
            }

            override fun onAdShow(p0: View?, p1: Int) {
                adListener.onAdShowed(null)
            }
        })
        if (adObj.interactionType == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            adObj.setDownloadListener(downloadListener)
        }
        adObj.setShowDislikeIcon(dislikeCallback)
        return adObj.bannerView
    }

    fun getSplashAd(notAllowSdkCountdown: Boolean = false,
                    downloadListener: TTAppDownloadListener? = null): View {
        check(adObj is TTSplashAd) { "adObj is not TTSplashAd" }
        adObj.setSplashInteractionListener(object : TTSplashAd.AdInteractionListener {
            override fun onAdClicked(p0: View?, p1: Int) {

                adListener.onAdClicked(null)
            }

            override fun onAdSkip() {
                adListener.onAdClosed(null)
            }

            override fun onAdShow(p0: View?, p1: Int) {

                adListener.onAdShowed(null)
            }

            override fun onAdTimeOver() {
            }
        })
        adObj.setDownloadListener(downloadListener)
        if (notAllowSdkCountdown) {
            adObj.setNotAllowSdkCountdown()
        }
        return adObj.splashView
    }

    fun showInteractionAd(activity: Activity, downloadListener: TTAppDownloadListener? = null) {
        check(adObj is TTInteractionAd) { "adObj is not TTInteractionAd" }
        adObj.setAdInteractionListener(object : TTInteractionAd.AdInteractionListener {
            override fun onAdDismiss() {
                adListener.onAdClosed(null)
            }

            override fun onAdClicked() {

                adListener.onAdClicked(null)
            }

            override fun onAdShow() {

                adListener.onAdShowed(null)
            }
        })
        if (adObj.interactionType == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            adObj.setDownloadListener(downloadListener)
        }
        adObj.showInteractionAd(activity)
    }

    fun showFullScreenVideoAd(activity: Activity, downloadListener: TTAppDownloadListener? = null) {
        check(adObj is TTFullScreenVideoAd) { "adObj is not TTFullScreenVideoAd" }
        adObj.setFullScreenVideoAdInteractionListener(object : TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {
            override fun onSkippedVideo() {
            }

            override fun onAdShow() {

                adListener.onAdShowed(null)

            }

            override fun onAdVideoBarClick() {

                adListener.onAdClicked(null)
            }

            override fun onVideoComplete() {
                adListener.onVideoPlayFinish(null)
            }

            override fun onAdClose() {
                adListener.onAdClosed(null)
            }
        })
        if (adObj.interactionType == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            adObj.setDownloadListener(downloadListener)
        }
        adObj.showFullScreenVideoAd(activity)
    }

    fun showVideoAd(activity: Activity, downloadListener: TTAppDownloadListener? = null) {
        check(adObj is TTRewardVideoAd) { "adObj is not TTRewardVideoAd" }
        adObj.setRewardAdInteractionListener(object : TTRewardVideoAd.RewardAdInteractionListener {
            override fun onRewardVerify(p0: Boolean, p1: Int, p2: String?) {
            }

            override fun onSkippedVideo() {
            }

            override fun onAdShow() {

                adListener.onAdShowed(null)
            }

            override fun onAdVideoBarClick() {

                adListener.onAdClicked(null)
            }

            override fun onVideoComplete() {
                adListener.onVideoPlayFinish(null)
            }

            override fun onAdClose() {
                adListener.onAdClosed(null)
            }

            override fun onVideoError() {
            }
        })
        if (adObj.interactionType == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            adObj.setDownloadListener(downloadListener)
        }
        adObj.showRewardVideoAd(activity)
    }

    fun showNativeExpressAd(adIndex: Int, container: ViewGroup) {

        val ad = when (adObj) {
            is MutableList<*> -> adObj[adIndex]
            else -> {
                if (adIndex == 0) {
                    adObj
                } else {
                    null
                }
            }
        }

        val view = views!![adIndex]

        check(ad is TTNativeExpressAd) { "ad is not TTNativeExpressAd" }

        ad.setExpressInteractionListener(object : TTNativeExpressAd.AdInteractionListener {
            override fun onAdDismiss() {
                adListener.onAdClosed(null)
            }

            override fun onAdClicked(p0: View?, p1: Int) {
                adListener.onAdClicked(null)
            }

            override fun onAdShow(p0: View?, p1: Int) {
                adListener.onAdShowed(null)
            }

            override fun onRenderSuccess(view: View?, width: Float, height: Float) {
            }

            override fun onRenderFail(p0: View?, p1: String?, p2: Int) {
            }
        })

        val parent = view.parent
        if (parent is ViewGroup) {
            parent.removeAllViews()
        }

        container.addView(view)
        container.visibility = View.VISIBLE
    }
}