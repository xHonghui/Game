package com.nft.quizgame.common.ad

import android.app.Activity
import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bytedance.sdk.openadsdk.*
import com.cs.bd.ad.AdSdkApi
import com.cs.bd.ad.bean.AdModuleInfoBean
import com.cs.bd.ad.http.bean.BaseModuleDataItemBean
import com.cs.bd.ad.manager.AdSdkManager
import com.cs.bd.ad.params.AdSdkParamsBuilder
import com.cs.bd.ad.sdk.GdtAdCfg
import com.cs.bd.ad.sdk.TouTiaoAdCfg
import com.cs.bd.ad.sdk.bean.SdkAdSourceAdWrapper
import com.nft.quizgame.common.QuizAppState
import com.nft.quizgame.common.R
import com.nft.quizgame.common.buychannel.AppsFlyProxy
import com.nft.quizgame.common.buychannel.BuyChannelApiProxy
import com.nft.quizgame.common.event.AdLoadEvent
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.pref.PrivatePreference
import com.nft.quizgame.common.statistic.BaseSeq103OperationStatistic
import com.nft.quizgame.common.utils.DrawUtils
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.common.utils.WindowController
import com.nft.quizgame.ext.post
import com.nft.quizgame.ext.postDelayed
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD
import com.qq.e.ads.nativ.NativeExpressADView
import com.qq.e.ads.rewardvideo.RewardVideoAD
import com.qq.e.ads.splash.SplashAD
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AdController {

    const val TAG = "AdController"

    const val KEY_LAST_DISPLAY_TIME_PREFIX = "key_last_display_time_"


    private val adBeanMap = SparseArray<AdBean>()

    private val dilutionAdBeanMap = SparseArray<AdBean>()

    private val mAdLoadLiveDataList = HashMap<Int, MutableLiveData<Event<AdLoadEvent>>>()

    private val TT_APP_ID = "5060044"
    val ttAdConfig: TTAdConfig = TTAdConfig.Builder()
            .appId(TT_APP_ID)
            .useTextureView(true) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
            .appName(QuizAppState.getContext().resources?.getString(R.string.app_name))
            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
            .allowShowNotify(false) //是否允许sdk展示通知栏提示
            .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
            .debug(true) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
            .directDownloadNetworkType(
                    TTAdConstant.NETWORK_STATE_WIFI,
                    TTAdConstant.NETWORK_STATE_3G
            ) //允许直接下载的网络状态集合
            .supportMultiProcess(true) //是否支持多进程，true支持
            .build()


    /**
     * 加载广告
     * 若对应moduleId的广告正在加载中，则不重复加载，返回false
     */

    fun loadAd(param: LoadAdParameter): Boolean {
        val id = param.moduleId

        val adBean = getAdBean(id, adBeanMap)
        if (adBean != null) {
            if (adBean.isLoading.value!!) {
                return false
            } else if (adBean.adData != null && !adBean.isShown && !adBean.isOutDate) {
                getAdLoadLiveData(adBean.moduleId).value = Event(AdLoadEvent.OnAdLoadSuccess(adBean.moduleId))
                return false
            }
        }


        if (param.adInterceptor != null && !param.adInterceptor!!.isLoadAd(id, param.timeGapLimit)) {
            return false
        }

        return doLoadAd(param.context, id, param.feedViewWidth, param.adCount, adBeanMap, param.splashContainer, param.virtualModuleIdConverter, param.entrance)
    }

    @Synchronized
    fun getAdLoadLiveData(moduleId: Int): MutableLiveData<Event<AdLoadEvent>> {
        var liveData = mAdLoadLiveDataList[moduleId]
        if (liveData == null) {
            liveData = MutableLiveData()
            mAdLoadLiveDataList[moduleId] = liveData
        }
        return liveData
    }

    private fun doLoadAd(
            context: Context, moduleId: Int, feedViewWidth: Int, adCount: Int = 1,
            beanMap: SparseArray<AdBean>, container: ViewGroup?,
            converter: VirtualModuleIdConverter?,
            entrance: String): Boolean {
        val adBean = AdBean(moduleId)
        beanMap.put(moduleId, adBean)
        adBean.isLoading.value = true

        val adListener = object : AdSdkManager.IVLoadAdvertDataListener {

            override fun onVideoPlayFinish(p0: Any?) {
                Logcat.d(TAG, "onVideoPlayFinish moduleId = $moduleId")

                AppsFlyProxy.uploadRewardVideoDone()
                adBean.interactionListener?.onVideoPlayFinished()
                BaseSeq103OperationStatistic.uploadAdEnd(moduleId, entrance)
            }

            override fun onAdImageFinish(p0: AdModuleInfoBean?) {
                Logcat.d(TAG, "onAdImageFinish moduleId = $moduleId")
            }

            override fun onAdClicked(p0: Any?) {
                Logcat.d(TAG, "onAdClicked moduleId = $moduleId")

                adBean.adData?.uploadClick(QuizAppState.getContext())

                adBean.interactionListener?.onAdClicked()

                BaseSeq103OperationStatistic.uploadAdClick(moduleId, entrance)
            }

            override fun onAdInfoFinish(p0: Boolean, adData: AdModuleInfoBean?) {

                Logcat.d(TAG, "onAdInfoFinish moduleId = $moduleId")
                if (adData == null) {
                    onAdFail(-1)
                    return
                }


                val adViewList = adData.sdkAdSourceAdInfoBean.adViewList
                val adView: Any
                val adObj: Any = if (adViewList.size > 1) {
                    val adViewObjList = ArrayList<Any>(adViewList.size)
                    for (sdkAdSourceAdWrapper in adViewList) {
                        adViewObjList.add(sdkAdSourceAdWrapper.adObject)
                    }
                    adView = adViewObjList[0]
                    adViewObjList

                } else {
                    val adObject = adViewList[0].adObject
                    adView = adObject
                    adObject
                }

                val adStyle: Int = when (adView) {
                    is TTNativeAd -> AdStyle.NATIVE_INFO_FLOW
                    is TTNativeExpressAd -> AdStyle.NATIVE
                    is TTBannerAd -> AdStyle.BANNER
                    is TTSplashAd -> AdStyle.SPLASH
                    is TTFullScreenVideoAd -> AdStyle.FULL_SCREEN_VIDEO
                    is TTInteractionAd -> AdStyle.FULL_SCREEN
                    is TTRewardVideoAd -> AdStyle.REWARD_VIDEO


                    is SplashAD -> AdStyle.SPLASH
                    is UnifiedInterstitialAD -> AdStyle.FULL_SCREEN
                    is NativeExpressADView -> AdStyle.NATIVE
                    is RewardVideoAD -> AdStyle.REWARD_VIDEO

                    else -> AdStyle.NATIVE
                }


                if (adView !is TTNativeExpressAd) {
                    onAdLoadSuccess(adData, adObj, adStyle, adViewList, null)
                    return
                }

                Logcat.d(TAG, "renderNativeExpressAd")
                renderNativeExpressAd(adObj) {
                    if (it.isEmpty()) {
                        Logcat.d(TAG, "renderNativeExpressAd fail")
                        onAdFail(-1)
                        return@renderNativeExpressAd
                    }

                    Logcat.d(TAG, "renderNativeExpressAd success")
                    val adList = ArrayList<TTNativeExpressAd>()
                    val viewList = ArrayList<View>()
                    for (item in it) {
                        adList.add(item.adObj)
                        viewList.add(item.view!!)
                    }

                    onAdLoadSuccess(adData, adList, adStyle, adViewList, viewList)

                }
            }

            private fun onAdLoadSuccess(adData: AdModuleInfoBean, adObj: Any, adStyle: Int, sdkAdSourceAdWrapperList: MutableList<SdkAdSourceAdWrapper>, views: List<View>?) {
                Logcat.d(TAG, "onAdLoadSuccess moduleId = $moduleId")
                post {
                    when (adData.moduleDataItemBean.advDataSource) {
                        BaseModuleDataItemBean.AD_DATA_SOURCE_TOU_TIAO -> {
                            adBean.adData = TTAdData(
                                    adObj, adData.moduleDataItemBean.advDataSource, adStyle,
                                    adData.sdkAdControlInfo, sdkAdSourceAdWrapperList[0], this, views)
                        }
                        BaseModuleDataItemBean.AD_DATA_SOURCE_GDT -> {
                            adBean.adData = GDTAdData(
                                    adObj, adData.moduleDataItemBean.advDataSource, adStyle,
                                    adData.sdkAdControlInfo, sdkAdSourceAdWrapperList[0], this)
                        }
                    }

                    adBean.isLoading.value = false
                    getAdLoadLiveData(adBean.moduleId).value = Event(AdLoadEvent.OnAdLoadSuccess(adBean.moduleId))
                    BaseSeq103OperationStatistic.uploadAdFilled(moduleId, entrance)
                }

            }

            override fun onAdShowed(p0: Any?) {
                Logcat.d(TAG, "onAdShowed moduleId = $moduleId")
                adBean.adData?.uploadShow(QuizAppState.getContext())
                adBean.isShown = true
                val pref = PrivatePreference.getPreference()
                pref.putValue("$KEY_LAST_DISPLAY_TIME_PREFIX$moduleId", System.currentTimeMillis())
                pref.apply()
                adBean.interactionListener?.onAdShowed()
                BaseSeq103OperationStatistic.uploadAdShow(moduleId, entrance)
            }

            override fun onAdClosed(p0: Any?) {
                Logcat.d(TAG, "onAdClosed moduleId = $moduleId")
                adBean.interactionListener?.onAdClosed()
                BaseSeq103OperationStatistic.uploadAdClose(moduleId, entrance)
            }

            override fun onAdFail(statusCode: Int) {
                Logcat.d(TAG, "onAdFail moduleId = $moduleId ,statusCode = $statusCode")
                post {
                    adBean.isLoading.value = false
                    beanMap.remove(moduleId)
                    getAdLoadLiveData(moduleId).value = Event(AdLoadEvent.OnAdLoadFail(moduleId, statusCode))
                }
            }

        }

        loadSimpleAd(context, moduleId, adCount, null, container, feedViewWidth, adListener, converter) { _ -> }

        BaseSeq103OperationStatistic.uploadAdRequest(moduleId, entrance)

        Logcat.d(TAG, "loadAD moduleId = $moduleId")
        return true
    }

    private fun loadSimpleAd(
            context: Context, moduleId: Int, adCount: Int,
            adControlInterceptor: AdSdkManager.IAdControlInterceptor?, container: ViewGroup?,
            feedViewWidth: Int, listener: AdSdkManager.ILoadAdvertDataListener,
            converter: VirtualModuleIdConverter?, preLoad: (AdSdkParamsBuilder.Builder) -> Unit) {
        val convertVirtualModuleId = converter?.convertToVirtualModuleId(context, moduleId)
                ?: moduleId
        Logcat.d(TAG, "loadAD convertVirtualModuleId = $convertVirtualModuleId")
        val builder = AdSdkParamsBuilder.Builder(context, convertVirtualModuleId, null, listener)
        preLoad(builder)

        val gdtAdCfg = GdtAdCfg()

        gdtAdCfg.isUseNativeAdExpress = true
        if (container != null) {
            gdtAdCfg.splashCfg = GdtAdCfg.SplashCfg(container)
        }


        val feedViewWidthPx = if (feedViewWidth > 0) {
            feedViewWidth
        } else {
            WindowController.getScreenWidth()
        }

        val adslotBuilder = AdSlot.Builder()
                .setAdCount(adCount)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(WindowController.getScreenWidth(), WindowController.getScreenHeight())
        if (container == null) {
            adslotBuilder.setExpressViewAcceptedSize(DrawUtils.px2dip(feedViewWidthPx.toFloat()).toFloat(), 0f)//dp 高=0会自适应
        }

        val adSlop = adslotBuilder
                .setOrientation(TTAdConstant.VERTICAL)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build() //以后通过网络配置

//        val adSlop = AdSlot.Builder()
//                .setSupportDeepLink(true)
//                .setImageAcceptedSize(DensityUtil.getScreenWidth(context), DensityUtil.getScreenHeight(context)) //以后通过网络配置
//                .setOrientation(TTAdConstant.VERTICAL)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
//                .build()
        val touTiaoAdCfg = TouTiaoAdCfg(adSlop)
        touTiaoAdCfg.isUseBannerAdExpress = true
        touTiaoAdCfg.isUseInterstitialAdExpress = true

        builder.adControlInterceptor(adControlInterceptor)
        val adBuilder = builder.returnAdCount(adCount)
                .isNeedDownloadIcon(true)
                .isNeedDownloadBanner(true)
                .isNeedPreResolve(true)
                .isRequestData(false)
                .buyuserchannel(BuyChannelApiProxy.buyChannel)
                .cdays(QuizAppState.getFacade().getCDays())
                .gdtAdCfg(gdtAdCfg)
                .touTiaoAdCfg(touTiaoAdCfg)
                .userFrom(BuyChannelApiProxy.secondUserType)
                .build()
        AdSdkApi.loadAdBean(adBuilder)

    }


    /**
     * 删除广告AdBean
     */
    fun removeAdBean(moduleId: Int, isDiluteAd: Boolean = false) {
        val beanMap = if (isDiluteAd) dilutionAdBeanMap else adBeanMap
        beanMap.remove(moduleId)
    }

    /**
     * 获取已经准备好的广告AdBean
     * 对应moduleId没有发起过加载或正在加载中，返回null
     * 广告过时或已经被展示过的，返回null
     */
    fun getPendingAdBean(moduleId: Int, isDiluteAd: Boolean = false): AdBean? {
        val beanMap = if (isDiluteAd) dilutionAdBeanMap else adBeanMap
        val adBean = getAdBean(moduleId, beanMap)
        if (adBean != null) {
            if (adBean.adData == null || adBean.isLoading.value!!) {
                return null
            } else if (adBean.isOutDate || adBean.isShown) {
                beanMap.remove(moduleId)
                return null
            }
        }
        beanMap.remove(moduleId)
        return adBean
    }

    private fun getAdBean(moduleId: Int, beanMap: SparseArray<AdBean>): AdBean? {
        return beanMap[moduleId]
    }

    fun getAdLoadingData(moduleId: Int, isDiluteAd: Boolean = false): MutableLiveData<Boolean>? {
        val beanMap = if (isDiluteAd) dilutionAdBeanMap else adBeanMap
        val adBean = getAdBean(moduleId, beanMap)
        if (adBean != null) {
            return adBean.isLoading
        }
        return null
    }

    fun isAdLoading(moduleId: Int, isDiluteAd: Boolean = false): Boolean {
        val loadingData = getAdLoadingData(moduleId, isDiluteAd)
        return loadingData?.value ?: false
    }

    private fun getStatisticsTab(adData: AdData): Int {
        return when (adData) {
            is TTAdData -> 4
            is GDTAdData -> 5
            else -> 0
        }
    }

    fun showRewardVideo(activity: Activity, adBean: AdBean): Boolean {
        if (adBean.adData != null && !activity.isFinishing) {
            if (adBean.adData is TTAdData) {
                val ttAdData = adBean.adData as TTAdData
                ttAdData.showVideoAd(activity, null)
                return true
            } else if (adBean.adData is GDTAdData) {
                val gdtAdData = adBean.adData as GDTAdData
                gdtAdData.showRewardVideo(activity)
                return true
            }
        }
        return false
    }


    fun showInternalAd(parameter: ShowInternalAdParameter) {
        val adBean = parameter.adBean
        val activity = parameter.activity
        val container = parameter.container
        val dilutionViewGroup = parameter.dilutionViewGroup
//        val listener = parameter.listener
        val slideIntervalTimeBanner = parameter.slideIntervalTimeBanner

        val adData = adBean.adData
        if (adData == null) {
            Logcat.e(TAG, "showInternalAd parameter is null")
            return
        }

        if (adData is TTAdData) {
            when (adData.adStyle) {
                AdStyle.NATIVE_INFO_FLOW -> {
                    throw java.lang.IllegalArgumentException("not support NATIVE_INFO_FLOW")
                }
                AdStyle.NATIVE -> {
                    if (container == null) {
                        throw java.lang.IllegalArgumentException("container == null")
                    }
                    configNativeInfoFlowAd(adData, container, dilutionViewGroup)
                }
                AdStyle.FULL_SCREEN_VIDEO -> {
                    adData.showFullScreenVideoAd(activity!!, null)
                }
                AdStyle.BANNER -> {
                    if (container == null) {
                        throw IllegalArgumentException("container must not be null")
                    }
                    val view = adData.getBannerAd(slideIntervalTimeBanner)
                    val parent = view.parent
                    if (parent is ViewGroup) {
                        parent.removeAllViews()
                    }
                    container.removeAllViews()
                    container.addView(view)
                    container.visibility = View.VISIBLE
                }
            }
        } else if (adData is GDTAdData) {
            if (adData.adStyle == AdStyle.FULL_SCREEN) {
                adData.showInterstitialAd(activity)
            } else {
                if (container == null) {
                    throw IllegalArgumentException("container must not be null")
                }
                val list = adData.getNativeExpressAds()
                if (list.isNotEmpty()) {
                    val view = list[0]
                    view.render()

                    val parent = view.parent
                    if (parent is ViewGroup) {
                        parent.removeAllViews()
                    }

                    container.removeAllViews()
                    container.addView(view)
                    container.visibility = View.VISIBLE
                }
            }
        }

        /* val mAdAdapter = ActivityLifecycleCallbackAdAdapter()
         QuizAppState.getApplication().registerActivityLifecycleCallbacks(mAdAdapter);
         adBean.interactionListener = object : AdBean.AdInteractionListenerAdapter() {
             override fun onAdClosed() {
                 super.onAdClosed()
                 QuizAppState.getApplication().unregisterActivityLifecycleCallbacks(mAdAdapter)
                 if (adData is GDTAdData) {
                     val gdtAdData = adData as GDTAdData
                     if (gdtAdData.adStyle != AdStyle.FULL_SCREEN) {
                         container?.removeAllViews()
                         dilutionViewGroup?.removeAllViews()
                     }
                 }
                 listener?.onAdClosed()
             }

             override fun onAdClicked() {
                 super.onAdClicked()
                 if (adData is TTAdData) {
                     val ttAdData = adData as TTAdData
                     if (ttAdData.adStyle == AdStyle.NATIVE_INFO_FLOW || ttAdData.adStyle == AdStyle.NATIVE) {
                         container?.removeAllViews()
                         dilutionViewGroup?.removeAllViews()
                     } else if (ttAdData.adStyle == AdStyle.FULL_SCREEN_VIDEO) {
                         mAdAdapter.result?.finish()
                     }
                 } else if (adData is GDTAdData) {
                     val gdtAdData = adData as GDTAdData
                     if (gdtAdData.adStyle != AdStyle.FULL_SCREEN) {
                         container?.removeAllViews()
                         dilutionViewGroup?.removeAllViews()
                     }
                 }
                 listener?.onAdClicked()
             }

             override fun onVideoPlayFinished() {
                 super.onVideoPlayFinished()
                 listener?.onVideoPlayFinished()
             }

             override fun onAdShowed() {
                 super.onAdShowed()
                 listener?.onAdShowed()
             }
         }*/


    }

    fun showInterstitialAd(activity: Activity?, adBean: AdBean?, splashViewGroup: ViewGroup? = null) {
        if (adBean?.adData == null || activity == null || /* activity.isDestroyed ||*/ activity.isFinishing) {
            return
        }

        if (adBean.adData is TTAdData) {
            val ttAdData = adBean.adData as TTAdData

            if (ttAdData.adStyle == AdStyle.FULL_SCREEN) {
                ttAdData.showInteractionAd(activity)
            } else if (ttAdData.adStyle == AdStyle.FULL_SCREEN_VIDEO) {
                ttAdData.showFullScreenVideoAd(activity, null)
            } else if (ttAdData.adStyle == AdStyle.NATIVE) {
                val popupAdDialog = PopupAdDialog(activity, adBean)
                popupAdDialog.show()

                popupAdDialog.setOnDismissListener { adBean.interactionListener?.onAdClosed() }

            } else if (ttAdData.adStyle == AdStyle.SPLASH) {
                if (splashViewGroup != null) {
                    showSplash(adBean, splashViewGroup)
                }
            }

        } else if (adBean.adData is GDTAdData) {
            val gdtAdData = adBean.adData as GDTAdData
            if (gdtAdData.adStyle == AdStyle.FULL_SCREEN) {
                gdtAdData.showInterstitialAd(activity)
            } else if (gdtAdData.adStyle == AdStyle.NATIVE) {
                val popupAdDialog = PopupAdDialog(activity, adBean)
                popupAdDialog.show()

                popupAdDialog.setOnDismissListener { adBean.interactionListener?.onAdClosed() }

            }
        }

    }


    fun configExternalAd(activity: Activity?, adBean: AdBean?, container: ViewGroup,
                         dilutionViewGroup: ViewGroup? = null) {

        if (adBean?.adData == null || activity == null || /* activity.isDestroyed ||*/ activity.isFinishing) {
            return
        }
        container.visibility = View.GONE
        dilutionViewGroup?.visibility = View.GONE

        if (adBean.adData is TTAdData) {
            val ttAdData = adBean.adData as TTAdData
            if (ttAdData.adStyle == AdStyle.NATIVE_INFO_FLOW || ttAdData.adStyle == AdStyle.NATIVE) {
                configNativeInfoFlowAd(ttAdData, container, dilutionViewGroup)

            }
        } else if (adBean.adData is GDTAdData) {
            val gdtAdData = adBean.adData as GDTAdData
            if (gdtAdData.adStyle == AdStyle.FULL_SCREEN) {
                gdtAdData.showInterstitialAd(activity)
            } else {
                val list = gdtAdData.getNativeExpressAds()
                if (list.isNotEmpty()) {
                    val view = list[0]
                    view.render()

                    val parent = view.parent
                    if (parent is ViewGroup) {
                        parent.removeAllViews()
                    }

                    container.removeAllViews()
                    container.addView(view)
                    container.visibility = View.VISIBLE
                }
            }
        }
    }


    //true 加载广告|false 触发间隔
    fun intervalRefreshAd(intervalTimeMillis: Long, life: LifecycleOwner): MutableLiveData<Boolean> {
        val mutableLiveData = MutableLiveData<Boolean>()
        mutableLiveData.value = true

        mutableLiveData.observe(life, androidx.lifecycle.Observer {
            if (it == false) {
                Logcat.d("intervalRefreshAd", "postDelayed")
                postDelayed(intervalTimeMillis) {
                    mutableLiveData.value = true
                }

            }
        })

        return mutableLiveData
    }


    fun configNativeInfoFlowAd(adData: TTAdData, adViewGroup: ViewGroup, dilutionViewGroup: ViewGroup? = null) {
        if (adData.adStyle != AdStyle.NATIVE) {
            throw java.lang.IllegalStateException("adData.adStyle != AdStyle.NATIVE")
        }

        val feedAdCount = adData.getAdCount()
        dilutionViewGroup?.visibility = View.GONE
        adViewGroup.visibility = View.GONE

        if (feedAdCount == 0) {
            return
        }

        if (feedAdCount >= 1) {
            adViewGroup.visibility = View.VISIBLE
            adViewGroup.removeAllViews()
        }

        if (feedAdCount > 1) {
            dilutionViewGroup?.visibility = View.VISIBLE
            dilutionViewGroup?.removeAllViews()
        }

        for (feedIndex in 0 until feedAdCount) {


            var viewGroup: ViewGroup?
            if (feedIndex == 0) {
                viewGroup = adViewGroup
            } else {
                if (dilutionViewGroup == null) {
                    continue
                }
                viewGroup = dilutionViewGroup
            }


            adData.showNativeExpressAd(feedIndex, viewGroup)
        }

    }

    fun showSplash(adBean: AdBean, splashContainer: ViewGroup): Boolean {
        val data = adBean.adData
        if (data is TTAdData) {
            val adView = data.getSplashAd(false, null)
            val parent = adView.parent
            if (parent is ViewGroup) {
                parent.removeAllViews()
            }
            splashContainer.addView(adView)
            return true
        } else if (data is GDTAdData) {
            data.fetchSplashAd(splashContainer)
            return true
        }
        return false
    }

    fun cancelLoad(moduleId: Int) {
        getAdBean(moduleId, adBeanMap)?.let { adBean ->
            adBean.isLoading.value = false
        }
    }


    fun renderNativeExpressAd(data: Any, slideIntervalTime: Int = 0, onRenderFinished: (result: List<RenderNativeExpressAdResult>) -> Unit) {

        val adObj: ArrayList<TTNativeExpressAd>
        if (data is TTNativeExpressAd) {
            adObj = ArrayList()
            adObj.add(data)
        } else {
            adObj = data as ArrayList<TTNativeExpressAd>
        }

        if (adObj.isEmpty()) {
            throw IllegalStateException("adObj.isEmpty()")
        }

        GlobalScope.launch {

            val resultList = ArrayList<RenderNativeExpressAdResult>(adObj.size)
            val list = ArrayList<Deferred<RenderNativeExpressAdResult?>>(adObj.size)

            for (ad in adObj) {
                try {
                    val async = async {
                        realRenderNativeExpressAd(ad, slideIntervalTime)
                    }
                    list.add(async)
                } catch (e: Exception) {
                    Logcat.d(TAG, e.message)
                }
            }

            for (item in list) {
                val element = item.await()
                if (element != null) {
                    resultList.add(element)
                }
            }

            onRenderFinished.invoke(resultList)

        }


    }

    private suspend fun realRenderNativeExpressAd(adObj: TTNativeExpressAd, slideIntervalTime: Int = 0) = suspendCoroutine<RenderNativeExpressAdResult?> { cont ->
        adObj.setSlideIntervalTime(slideIntervalTime)
        adObj.setExpressInteractionListener(object : TTNativeExpressAd.AdInteractionListener {
            override fun onAdDismiss() {
            }

            override fun onAdClicked(p0: View?, p1: Int) {

            }

            override fun onAdShow(p0: View?, p1: Int) {

            }

            override fun onRenderSuccess(view: View?, width: Float, height: Float) {
                cont.resume(RenderNativeExpressAdResult(view, adObj))
            }

            override fun onRenderFail(p0: View?, p1: String?, p2: Int) {
                cont.resume(null)
            }
        })

        post {
            adObj.render()
        }
    }

    class RenderNativeExpressAdResult(val view: View?, val adObj: TTNativeExpressAd)
}
