package com.nft.quizgame.dialog

import android.view.View
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.quiz_dialog.*

class CoinAnimationDialogHelper(private val dialog: QuizDialog<*>, private val coinAnimEndLoc: IntArray,
                                private val coinAnimObserver: Observer<Float>,
                                private val animationEndCallback: ((Int) -> Unit)? = null) {

    private var isPendingToDismiss = false
    private val smallCoinLoc = intArrayOf(0, 0)

    private fun getCoinAnimStartCoordinate(): IntArray {
        if (smallCoinLoc[0] > 0 && smallCoinLoc[1] > 0) {
            return smallCoinLoc
        }
        dialog.img_logo.getLocationInWindow(smallCoinLoc)
        smallCoinLoc[0] += (dialog.img_logo.width / 2f).toInt()
        smallCoinLoc[1] += (dialog.img_logo.height / 2f).toInt()
        return smallCoinLoc
    }

    fun startCoinAnimation(earnBonus: Int, coinCount:Int) {
        dialog.coin_anim_layer.visibility = View.VISIBLE
        val startLoc = getCoinAnimStartCoordinate()
        val endLoc = coinAnimEndLoc
        val mod = earnBonus % coinCount
        val bonusPerCoin = earnBonus / coinCount.toFloat()
        val bonusArray = FloatArray(coinCount)
        for (i in 0 until coinCount) {
            if (i == coinCount - 1) {
                bonusArray[i] = bonusPerCoin + mod
            } else {
                bonusArray[i] = bonusPerCoin
            }
        }
        if (!dialog.coin_anim_layer.animationStateData.hasActiveObservers()) {
            dialog.coin_anim_layer.animationStateData.observeForever(coinAnimObserver)
        }
        dialog.coin_anim_layer.startCoinAnimation(startLoc[0], startLoc[1], endLoc[0], endLoc[1], bonusArray) {
            animationEndCallback?.invoke(earnBonus)
            if (isPendingToDismiss) {
                isPendingToDismiss = false
                dialog.dismiss(true)
            }
        }
    }

    fun onDismiss() {
        dialog.coin_anim_layer.animationStateData.removeObserver(coinAnimObserver)
    }

    fun dismiss() {
        if (dialog.coin_anim_layer.isAnimating()) {
            isPendingToDismiss = true
        } else {
            dialog.dismiss(true)
        }
    }
}