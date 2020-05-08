package com.nft.quizgame.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.CycleInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.ad.QuizLoadAdParameter
import com.nft.quizgame.common.ad.AdBean
import com.nft.quizgame.common.ad.AdController
import com.nft.quizgame.common.ad.ShowInternalAdParameter
import com.nft.quizgame.common.dialog.BaseDialog
import com.nft.quizgame.common.event.AdLoadEvent
import com.nft.quizgame.common.event.Event
import com.nft.quizgame.common.utils.DrawUtils
import com.nft.quizgame.config.bean.AdConfigBean
import com.nft.quizgame.config.ConfigManager
import kotlinx.android.synthetic.main.quiz_dialog.*
import kotlinx.android.synthetic.main.quiz_dialog_custom_container.*
import kotlinx.android.synthetic.main.quiz_dialog_default_container.*
import kotlinx.coroutines.runBlocking

abstract class QuizDialog<T : QuizDialog<T>>(activity: Activity, private val adModuleId: Int = -1,val mAdEntrance :String = "") :
        BaseDialog<T>(activity) {

    companion object{
        const val mNumberTextSize = 18
    }

    protected var coinAnimationHelper:CoinAnimationDialogHelper? = null
    protected val animationDialogHelper = QuizAnimationDialogHelper()

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.quiz_dialog)
    }

    private val mAdLoadObserver = Observer<Event<AdLoadEvent>> {
        it.peekContent().let { adLoadEvent ->
            if (adModuleId != adLoadEvent.adBeanModuleId) {
                return@let
            }

            when (adLoadEvent) {
                is AdLoadEvent.OnAdLoadSuccess -> {
                    AdController.getPendingAdBean(adLoadEvent.adBeanModuleId)
                            ?.let { pendingAdBean -> addAdView(pendingAdBean) }
                }

                is AdLoadEvent.OnAdLoadFail -> {

                }
            }
        }
    }

    override fun isFullScreenTransparent(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quiz_dialog_container.dialog = this
        AdController.getAdLoadLiveData(adModuleId).observe(this,mAdLoadObserver)
        runBlocking {
            val configBean = ConfigManager.getInstance().getConfigBean(
                    AdConfigBean.SID) as AdConfigBean
            if (configBean.isDialogBottomAdOpened && adModuleId != -1) {
                fl_ad_container.post {
                    val adBean = AdController.getPendingAdBean(adModuleId)
                    if (adBean != null) {
                        addAdView(adBean)
                    } else {
                        AdController.loadAd(QuizLoadAdParameter(activity, adModuleId).apply {
                            feedViewWidth = content_layout.width - fl_ad_container.paddingLeft - fl_ad_container.paddingRight
                            entrance = mAdEntrance
                        })
                    }
                }
            }
        }
    }

    override fun doDismiss() {
        super.doDismiss()
        animationDialogHelper.clearAllAnimation()
    }

    private fun addAdView(adBean: AdBean) {
        val lp = dialog_container.layoutParams as FrameLayout.LayoutParams
        lp.topMargin = DrawUtils.dip2px(40f)
        link.visibility = View.VISIBLE
        fl_ad_container.visibility = View.VISIBLE
        AdController.showInternalAd(ShowInternalAdParameter(activity, adBean, fl_ad_container))
    }

    fun logo(resId: Int? = null, jsonId: Int? = null, width: Int = -1, height: Int = -1): T {
        img_logo.visibility = View.VISIBLE
        logo_space.visibility = View.VISIBLE
        if (resId != null) {
            img_logo.setImageResource(resId)
        } else if (jsonId != null) {
            if (width != -1 && height != -1) {
                img_logo.layoutParams.width = width
                img_logo.layoutParams.height = height
            }
            img_logo.setAnimation(jsonId)
        }
        return this as T
    }

    fun logoBg(resId: Int, isAnim: Boolean = false): T {
        img_logo_bg.visibility = View.VISIBLE
        img_logo_bg.setImageResource(resId)
        if (isAnim){
            startLogoBgAnim(img_logo_bg)
        }
        return this as T
    }

    private fun startLogoBgAnim(view:View) {
        val rotateAnim = AnimationUtils.loadAnimation(context, R.anim.bg_rotate_anim)
        rotateAnim.repeatCount = Animation.INFINITE
        rotateAnim.interpolator = LinearInterpolator()
        view.startAnimation(rotateAnim)
        animationDialogHelper.addAnimation(rotateAnim)
    }

    open fun confirmButton(textId: Int? = null, text: CharSequence? = null, callback: ((Dialog) -> Unit)? = null): T {
        sl_ok.visibility = View.VISIBLE
        if (textId != null) {
            btn_ok.setText(textId)
        } else if (text != null) {
            btn_ok.text = text
        }
        callback?.let {
            btn_ok.setOnClickListener {
                isConfirmClicked = true
                it(this)
            }
        }
        startAnim(sl_ok)
        return this as T
    }

    private fun startAnim(view: View) {
        if (sl_cancel.isVisible && sl_ok.isVisible) {
            val scaleAnim = AnimationUtils.loadAnimation(context, R.anim.btn_scale_anim)
            val overshootInterpolator = CycleInterpolator(0.5f)
            scaleAnim.repeatMode = Animation.REVERSE
            scaleAnim.repeatCount = Animation.INFINITE
            scaleAnim.interpolator = overshootInterpolator
            view.startAnimation(scaleAnim)
            animationDialogHelper.addAnimation(scaleAnim)
        }
    }

    fun cancelButton(textId: Int? = null, text: CharSequence? = null, callback: ((Dialog) -> Unit)? = null): T {
        sl_cancel.visibility = View.VISIBLE
        if (textId != null) {
            btn_cancel.setText(textId)
        } else if (text != null) {
            btn_cancel.text = text
        }
        callback?.let {
            btn_cancel.setOnClickListener {
                isConfirmClicked = false
                it(this)
            }
        }
        startAnim(sl_ok)
        return this as T
    }

    fun title(textId: Int? = null, text: CharSequence? = null, textSize: Float = -1.0f): T {
        default_layout_stub?.visibility = View.VISIBLE
        custom_layout_stub?.visibility = View.GONE
        txt_title.visibility = View.VISIBLE
        if (textId != null) {
            txt_title.setText(textId)
        } else if (text != null) {
            txt_title.text = text
        }
        if (textSize != -1.0f) {
            txt_title.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        }
        return this as T
    }

    fun desc(textId: Int? = null, text: CharSequence? = null): T {
        default_layout_stub?.visibility = View.VISIBLE
        custom_layout_stub?.visibility = View.GONE
        txt_desc.visibility = View.VISIBLE
        if (textId != null) {
            txt_desc.setText(textId)
        } else if (text != null) {
            txt_desc.text = text
        }
        return this as T
    }

    fun closeButton(callback: ((Dialog) -> Unit)? = null): T {
        btn_close.visibility = View.VISIBLE
        callback?.let {
            btn_close.setOnClickListener {
                isConfirmClicked = false
                it(this)
            }
        }
        return this as T
    }

    fun customView(viewId: Int? = null, view: View? = null): T {
        default_layout_stub?.visibility = View.GONE
        custom_layout_stub?.visibility = View.VISIBLE
        var customView: View? = null
        if (viewId != null) {
            customView = LayoutInflater.from(activity).inflate(viewId, null)
        } else if (view != null) {
            customView = view
        }
        custom_layout_container.addView(customView)
        return this as T
    }

    fun startCoinAnimation(bonus: Int, coinCount: Int, coinAnimEndLoc: IntArray, coinAnimObserver: Observer<Float>,
                           animationEndCallback: ((Int) -> Unit)? = null) {
        coinAnimationHelper = CoinAnimationDialogHelper(this, coinAnimEndLoc, coinAnimObserver, animationEndCallback)
        coinAnimationHelper!!.startCoinAnimation(bonus, coinCount)
    }

    override fun dismiss() {
        if (coinAnimationHelper != null) {
            coinAnimationHelper!!.dismiss()
        } else {
            super.dismiss()
        }
    }

    override fun dismiss(invokeSuper: Boolean) {
        if (invokeSuper) {
            coinAnimationHelper?.onDismiss()
            super.dismiss()
        } else {
            dismiss()
        }
    }
}