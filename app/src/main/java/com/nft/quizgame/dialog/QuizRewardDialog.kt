package com.nft.quizgame.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizLoadAdParameter
import com.nft.quizgame.common.ad.AdBean
import com.nft.quizgame.common.ad.AdController
import com.nft.quizgame.common.ad.ShowInternalAdParameter
import com.nft.quizgame.common.event.AdLoadEvent
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.ext.toast
import kotlinx.android.synthetic.main.loading_view.*
import kotlinx.android.synthetic.main.quiz_dialog.*

open class QuizRewardDialog(activity: Activity, adModuleId: Int, protected val rewardModuleId: Int, entrance: String = "") :
        QuizDialog<QuizRewardDialog>(activity, adModuleId, entrance) {

    private var mRewardGainedCallback: ((Dialog) -> Unit)? = null
    private var mAdCloseCallback: ((Dialog, Boolean) -> Unit)? = null
    private var isRewardGained = false

    private val mRewardAdLoadObserver = Observer<Event<AdLoadEvent>> {
        it.peekContent().let { adLoadEvent ->
            if (adLoadEvent.adBeanModuleId == rewardModuleId) {
                when (adLoadEvent) {
                    is AdLoadEvent.OnAdLoadFail -> {
                        if (this@QuizRewardDialog.isShowing) {
                            toast(activity, R.string.reward_ad_load_fail)
                        }
                        loading_view.visibility = View.INVISIBLE
                    }

                    is AdLoadEvent.OnAdLoadSuccess -> {
                        if (this@QuizRewardDialog.isShowing) {
                            AdController.getPendingAdBean(adLoadEvent.adBeanModuleId)?.let { adBean ->
                                showRewardAd(adBean)
                            }
                        }
                        loading_view.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }


    override fun confirmButton(textId: Int?, text: CharSequence?, callback: ((Dialog) -> Unit)?): QuizRewardDialog {
        throw UnsupportedOperationException("Please use rewardButton instead.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdController.loadAd(QuizLoadAdParameter(activity, rewardModuleId).apply {  entrance = mAdEntrance })
    }

    fun rewardButton(textId: Int? = null, text: CharSequence? = null,
                     onClickCallback: (() -> Unit)? = null,
                     rewardGainedCallback: ((Dialog) -> Unit)? = null,
                     adCloseCallback: ((Dialog, Boolean) -> Unit)? = null): QuizRewardDialog {
        mRewardGainedCallback = rewardGainedCallback
        mAdCloseCallback = adCloseCallback
        val dialog = super.confirmButton(textId, text, null)
        btn_ok.setOnClickListener {
            onClickCallback?.invoke()
            val adBean = AdController.getPendingAdBean(rewardModuleId)
            if (adBean != null) {
                showRewardAd(adBean)
            } else {
                loading_view.visibility = View.VISIBLE
                AdController.loadAd(QuizLoadAdParameter(activity, rewardModuleId).apply {  entrance = mAdEntrance })
                AdController.getAdLoadLiveData(rewardModuleId).observe(this, mRewardAdLoadObserver)
            }
        }
        return dialog
    }

    private fun showRewardAd(adBean: AdBean) {
        adBean.interactionListener = object : AdBean.AdInteractionListenerAdapter() {
            override fun onVideoPlayFinished() {
                super.onVideoPlayFinished()
                if (!AppUtils.isStorePkg(context)) {
                    reward()
                }
            }

            override fun onAdShowed() {
                super.onAdShowed()
                if (AppUtils.isStorePkg(context)) {
                    reward()
                }
            }

            fun reward() {
                isConfirmClicked = true
                isRewardGained = true
                mRewardGainedCallback?.invoke(this@QuizRewardDialog)
            }

            override fun onAdClosed() {
                super.onAdClosed()
                mAdCloseCallback?.invoke(this@QuizRewardDialog, /*isRewardGained*/true)
            }
        }

        if (AppUtils.isStorePkg(context)) {
            AdController.showInternalAd(ShowInternalAdParameter(activity,adBean,null))
        } else {
            AdController.showRewardVideo(activity, adBean)
        }
    }
}