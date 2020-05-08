package com.nft.quizgame.function.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cs.bd.buychannel.IBuyChannelUpdateListener
import com.nft.quizgame.AppViewModelProvider
import com.nft.quizgame.BaseAppFragment
import com.nft.quizgame.MainActivity
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizAdConst
import com.nft.quizgame.ad.QuizLoadAdParameter
import com.nft.quizgame.common.ad.AdBean
import com.nft.quizgame.common.ad.AdController
import com.nft.quizgame.common.ad.AdStyle
import com.nft.quizgame.common.ad.TTAdData
import com.nft.quizgame.common.buychannel.BuyChannelApiProxy
import com.nft.quizgame.common.event.AdLoadEvent
import com.nft.quizgame.common.utils.Logcat
import com.nft.quizgame.common.utils.WindowController
import com.nft.quizgame.config.ConfigManager
import com.nft.quizgame.config.bean.AdConfigBean
import com.nft.quizgame.ext.post
import com.nft.quizgame.ext.postDelayed
import com.nft.quizgame.ext.removeCallbacks
import com.nft.quizgame.function.user.UserViewModel
import com.nft.quizgame.statistic.Statistic103
import kotlinx.android.synthetic.main.fragment_splash.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SplashFragment : BaseAppFragment() {

    private var mEnterApp = false
    private var mBgAnim: ObjectAnimator? = null
    private var mDelayTime = 8000L
    private var mStartTime = 0L

    private var mBuyChannelInitCompleted = false
    private var mAdInitCompleted = false
    private var mDelayCompleted = false

    private var mInitCompleted = false
    private var mAdIsShow = false
    var lastEnterAppStr = ""

    var mRunnable = Runnable {
        mDelayCompleted = true
        initFinish()
    }

    private val iBuyChannelUpdateListener = object : IBuyChannelUpdateListener {
        override fun onBuyChannelUpdate(p0: String?) {
            BuyChannelApiProxy.unregisterBuyChannelUpdateListener(this)
            post {
                mBuyChannelInitCompleted = true
                initFinish()
            }
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mStartTime = System.currentTimeMillis()
       /* animation_view.setOnClickListener {
            toast(it.context, lastEnterAppStr)
        }*/

        mBgAnim = ObjectAnimator.ofFloat(view_bg, "alpha", 0f, 1f)
        mBgAnim?.let {
            it.duration = 350
            it.startDelay = 2700
            it.start()
        }


        GlobalScope.launch(Main) {
            val configBean = ConfigManager.getInstance().getConfigBean(AdConfigBean.SID) as AdConfigBean
            if (!configBean.isSplashAdOpened) {
                setAdLoadCompleted()
                return@launch
            }

            if (mEnterApp) {
                return@launch
            }

            loadSplashAd()
            AdController.getAdLoadLiveData(QuizAdConst.SPLASH_AD_MODULE_ID).observe(viewLifecycleOwner, Observer { event ->
                event.getContentIfNotHandled()?.let { adLoadEvent ->
                    if (adLoadEvent.adBeanModuleId != QuizAdConst.SPLASH_AD_MODULE_ID) {
                        return@let
                    }

                    when (adLoadEvent) {
                        is AdLoadEvent.OnAdLoadSuccess -> {
                            showAd(AdController.getPendingAdBean(adLoadEvent.adBeanModuleId, false))
                            Logcat.d("SplashFragment", "showAd AdLoadEvent.OnAdLoadSuccess")
                        }

                        is AdLoadEvent.OnAdLoadFail -> {
                            setAdLoadCompleted()

                            if (splash_container.visibility == View.VISIBLE) {
                                post {
                                    splash_container?.removeAllViews()
                                    splash_container?.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            })


            BuyChannelApiProxy.registerBuyChannelUpdateListener(iBuyChannelUpdateListener)
            postDelayed(mDelayTime, mRunnable)
        }
    }

    private fun setAdLoadCompleted() {
        mAdInitCompleted = true
        initFinish()
    }

    private fun loadSplashAd() {
        val pendingAdBean = AdController.getPendingAdBean(QuizAdConst.SPLASH_AD_MODULE_ID, false)
        if (pendingAdBean != null) {
            val adData = pendingAdBean.adData
            if (adData is TTAdData) {
                splash_container.visibility = View.VISIBLE
                showAd(pendingAdBean)
                Logcat.d("SplashFragment", "showAd loadSplashAd")
                return
            } else {
                AdController.removeAdBean(QuizAdConst.SPLASH_AD_MODULE_ID, false)
            }
        }

        splash_container.visibility = View.VISIBLE
        AdController.loadAd(QuizLoadAdParameter(requireContext(), QuizAdConst.SPLASH_AD_MODULE_ID).apply {
            splashContainer = splash_container
        })
    }

    private fun showAd(adBean: AdBean?) {
        val adData = adBean?.adData
        if (adData == null) {
            setAdLoadCompleted()
            return
        }

        when (adData.adStyle) {
            AdStyle.SPLASH -> {
                Logcat.d("SplashFragment", "mAdIsShow = true")
                mAdIsShow = true
                fillSplash(adBean)
            }
        }

        setAdLoadCompleted()
    }

    private fun fillSplash(adBean: AdBean) {
        adBean.interactionListener = object : AdBean.AdInteractionListenerAdapter() {

            override fun onAdClosed() {
                processAdClose()
            }
        }

        AdController.showSplash(adBean, splash_container)
    }

    private fun processAdClose() {
        mAdIsShow = false
        splash_container.removeAllViews()
        enterApp()
    }

    override fun onResume() {
        super.onResume()
        if (mEnterApp) {
            enterApp(true)
        }
    }

    private fun initFinish() {

        if (mDelayCompleted || (mBuyChannelInitCompleted && mAdInitCompleted)) {
            if (!mInitCompleted) {
                //提交统计
                val requireActivity = requireActivity()
                if (requireActivity is MainActivity) {
                    Statistic103.uploadLaunchingShow((System.currentTimeMillis() - mStartTime) / 1000, requireActivity.mEnter, "${WindowController.getScreenWidth()}*${WindowController.getScreenHeight()}")
                }
            }
            mInitCompleted = true
        }

        enterApp()
    }

    private fun enterApp(force: Boolean = false) {
        lastEnterAppStr = "mBuyChannelInitCompleted:$mBuyChannelInitCompleted mAdInitCompleted:$mAdInitCompleted \r\n" +
                "mDelayCompleted:$mDelayCompleted \r\n" +
                "mAdIsShow = $mAdIsShow, mInitCompleted = $mInitCompleted"

        if (!force && (mAdIsShow || !mInitCompleted)) {
            return
        }

        removeCallbacks(mRunnable)
        BuyChannelApiProxy.unregisterBuyChannelUpdateListener(iBuyChannelUpdateListener)
        if (isDetached) {
            return
        }

        mEnterApp = true

        val map = findNavController().graph.arguments
        val get = AppViewModelProvider.getInstance().get(UserViewModel::class.java)
        if (get.userData.value == null) {
            navigate(R.id.action_to_login)
        } else {
            val navArgument = map[MainActivity.KEY_ENTER_FUNCTION]
            var bundle: Bundle? = null
            navArgument?.let {
                bundle = Bundle()
                bundle?.putString(MainActivity.KEY_ENTER_FUNCTION, it.defaultValue as String)
            }

            navigate(R.id.action_splash_to_main, bundle)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCallbacks(mRunnable)
        BuyChannelApiProxy.unregisterBuyChannelUpdateListener(iBuyChannelUpdateListener)
        mBgAnim?.cancel()
        animation_view?.cancelAnimation()
    }

    override fun onBackPressed(): Boolean {
        if (mAdIsShow) {
            processAdClose()
        }
        return true
    }

}