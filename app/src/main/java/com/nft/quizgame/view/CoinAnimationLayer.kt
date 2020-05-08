package com.nft.quizgame.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import com.nft.quizgame.R
import com.nft.quizgame.common.apng.PngUtils
import com.nft.quizgame.common.utils.DrawUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.cos
import kotlin.math.sin


class CoinAnimationLayer : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, style: Int) : super(context, attributeSet, style)

    companion object {
        private const val USE_APNG_ANIMATION = true
        const val DEFAULT_COIN_ANIMATION_COUNT = 6
    }

    private val coinSize = DrawUtils.dip2px(22f)
    val animationStateData = MutableLiveData<Float>(0f)
    private val coinCaches = LinkedList<AnimationDrawable>()

    init {
        GlobalScope.launch(IO) {
            for (i in 0 until DEFAULT_COIN_ANIMATION_COUNT) {
                val coinDrawable = PngUtils.readDrawable(context, R.raw.coin_animation) as AnimationDrawable
                withContext(Main) {
                    coinCaches.add(coinDrawable)
                }
            }
        }
    }

    fun startCoinAnimation(startX: Int, startY: Int, endX: Int, endY: Int, bonusArray: FloatArray,
                           animationEndCallback: () -> Unit) {
        GlobalScope.launch(Main) {
            val random = Random()
            bonusArray.forEach { bonus ->
                val delayFactor = 20L
                val delayMultiple = 1 + random.nextInt(15)
                val delay = delayFactor * delayMultiple
                val angleFactor = 20
                val angleMultiple = 1 + random.nextInt(18)
                val angle = angleFactor * angleMultiple
                val bevelEdge = DrawUtils.dip2px(10f) + random.nextInt(DrawUtils.dip2px(20f))
                val radians = Math.toRadians(angle.toDouble())
                val transferX = startX + cos(radians) * bevelEdge
                val transferY = startY + sin(radians) * bevelEdge
                val offsetX1 = transferX - startX
                val offsetY1 = transferY - startY
                val offsetX2 = endX - transferX
                val offsetY2 = endY - transferY

                val coinView = ImageView(context)
                var coinDrawable: AnimationDrawable? = null
                if (USE_APNG_ANIMATION) {
                    coinDrawable = if (coinCaches.isNotEmpty()) {
                        coinCaches.pop()
                    } else {
                        withContext(IO) {
                            PngUtils.readDrawable(context, R.raw.coin_animation) as AnimationDrawable
                        }
                    }
                    coinView.setImageDrawable(coinDrawable)
                } else {
                    coinView.setImageResource(R.mipmap.icon_money_small)
                }

                val param = LayoutParams(coinSize, coinSize)
                param.leftMargin = (startX - coinSize / 2.0f).toInt()
                param.topMargin = (startY - coinSize / 2.0f).toInt()
                addView(coinView, param)
                coinView.alpha = 0f
                coinView.animate().alpha(1f).translationXBy(offsetX1.toFloat()).translationYBy(offsetY1.toFloat())
                        .setDuration(300).setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                coinDrawable?.start()
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                post {
                                    coinView.animate().translationXBy(offsetX2.toFloat()).translationYBy(offsetY2.toFloat())
                                            .setInterpolator(AccelerateInterpolator())
                                            .setDuration(500).setListener(object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation: Animator?) {
                                                    animationStateData.value = bonus
                                                    post {
                                                        coinDrawable?.let {
                                                            it.stop()
                                                            if (coinCaches.size < DEFAULT_COIN_ANIMATION_COUNT) {
                                                                coinCaches.add(it)
                                                            }
                                                        }
                                                        removeView(coinView)
                                                        if (!isAnimating()) {
                                                            animationEndCallback.invoke()
                                                            animationStateData.value = 0f
                                                        }
                                                    }
                                                }
                                            }).start()
                                }
                            }
                        }).setStartDelay(delay).start()

            }
        }
    }

    fun isAnimating(): Boolean = childCount > 0
}