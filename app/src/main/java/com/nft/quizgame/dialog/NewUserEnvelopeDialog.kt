package com.nft.quizgame.dialog

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.common.dialog.BaseDialog
import com.nft.quizgame.common.utils.AppUtils
import com.nft.quizgame.dialog.view.QuizDialogContainer
import com.nft.quizgame.view.CoinAnimationLayer.Companion.DEFAULT_COIN_ANIMATION_COUNT
import kotlinx.android.synthetic.main.new_user_envelope_dialog.*

class NewUserEnvelopeDialog(activity: Activity, private val coinAnimEndLoc: IntArray,
                            private val coinAnimObserver: Observer<Float>,
                            private val envelopeCoin: Int,
                            private val openEnvelopeCallback: (Int) -> Unit) : BaseDialog<NewUserEnvelopeDialog>(activity) {

    private var isPendingToDismiss = false

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.new_user_envelope_dialog)
    }


    override fun isFullScreenTransparent(): Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = findViewById<QuizDialogContainer>(R.id.quiz_dialog_container)
        container.dialog = this

        btn_close.setOnClickListener {
            dismiss()
        }
        btn_get_it.setOnClickListener {
            dismiss()
        }
        txt_bonus.text = envelopeCoin.toString()
        txt_cash_desc.text = AppUtils.convertCoin(activity, envelopeCoin)
        txt_bonus.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                startCoinAnimation(envelopeCoin)
                txt_bonus.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private val smallCoinLoc = intArrayOf(0, 0)

    private fun getCoinAnimStartCoordinate(): IntArray {
        if (smallCoinLoc[0] > 0 && smallCoinLoc[1] > 0) {
            return smallCoinLoc
        }
        txt_bonus.getLocationInWindow(smallCoinLoc)
        smallCoinLoc[0] += (txt_bonus.width / 2f).toInt()
        smallCoinLoc[1] += (txt_bonus.height / 2f).toInt()
        return smallCoinLoc
    }

    private fun startCoinAnimation(earnCoin: Int) {
        coin_anim_layer.visibility = View.VISIBLE
        val startLoc = getCoinAnimStartCoordinate()
        val endLoc = coinAnimEndLoc
        val coinCount = DEFAULT_COIN_ANIMATION_COUNT * 2
        val mod = earnCoin % coinCount
        val bonusPerCoin = earnCoin / coinCount.toFloat()
        val bonusArray = FloatArray(coinCount)
        for (i in 0 until coinCount) {
            if (i == coinCount - 1) {
                bonusArray[i] = bonusPerCoin + mod
            } else {
                bonusArray[i] = bonusPerCoin
            }
        }
        if (!coin_anim_layer.animationStateData.hasActiveObservers()) {
            coin_anim_layer.animationStateData.observeForever(coinAnimObserver)
        }
        coin_anim_layer.startCoinAnimation(startLoc[0], startLoc[1], endLoc[0], endLoc[1], bonusArray) {
            openEnvelopeCallback.invoke(envelopeCoin)
            if (isPendingToDismiss) {
                isPendingToDismiss = false
                coin_anim_layer.animationStateData.removeObserver(coinAnimObserver)
                super.dismiss()
            }
        }
    }

    override fun dismiss() {
        if (coin_anim_layer.isAnimating()) {
            isPendingToDismiss = true
        } else {
            coin_anim_layer.animationStateData.removeObserver(coinAnimObserver)
            super.dismiss()
        }
    }

    override fun dismiss(invokeSuper: Boolean) {
        if (invokeSuper) {
            coin_anim_layer.animationStateData.removeObserver(coinAnimObserver)
            super.dismiss()
        } else {
            dismiss()
        }
    }

    override fun showPriority(): Int {
        return 5
    }
}