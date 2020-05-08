package com.nft.quizgame.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.animation.CycleInterpolator
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.common.utils.DrawUtils
import com.nft.quizgame.function.user.bean.UserBean

class CoinPolymericView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

    var user: UserBean? = null

    private var isCoinAnimating = false
    private val coinAnimListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            this@CoinPolymericView.translationY = 0f
            isCoinAnimating = false
        }
    }

    val coinAnimObserver = Observer<Float> { bonus ->
        if (bonus > 0) {
            user?.apply {
                coinAnim += bonus.toInt()
            }
            if (!isCoinAnimating) {
                isCoinAnimating = true
                this@CoinPolymericView.animate().translationYBy(DrawUtils.dip2px(3f).toFloat())
                        .setInterpolator(CycleInterpolator(1f)).setDuration(200).setListener(coinAnimListener).start()
            }
        }
    }

    private val bigCoinLoc = intArrayOf(0, 0)

    fun getImageCoinCoordinate(): IntArray {
        if (bigCoinLoc[0] > 0 && bigCoinLoc[1] > 0) {
            return bigCoinLoc
        }
        val imgCoin = findViewById<ImageView>(R.id.img_coin)
        imgCoin.getLocationInWindow(bigCoinLoc)
        bigCoinLoc[0] += (imgCoin.width / 2f).toInt()
        bigCoinLoc[1] += (imgCoin.height / 2f).toInt()
        return bigCoinLoc
    }
}