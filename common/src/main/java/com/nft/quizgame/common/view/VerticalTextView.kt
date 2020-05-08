package com.nft.quizgame.common.view

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewSwitcher
import java.util.*

/**
 * Created by xiehehe on 16/7/19.
 */
/**
 * yangguanxiang
 */
class VerticalTextView : TextSwitcher, ViewSwitcher.ViewFactory {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    companion object {
        private const val FLAG_START_AUTO_SCROLL = 0
        private const val FLAG_STOP_AUTO_SCROLL = 1
        private const val STATE_PAUSE = 2
        private const val STATE_SCROLL = 3
    }

    private var mTextSize = 16f
    private var mPadding = 5
    private var mTextColor = Color.BLACK
    private var mAnimateTime: Long = 0
    private var mScrollState = STATE_PAUSE
    private var mItemClickListener: OnItemClickListener? = null
    private var mCurrentId = -1
    private val mTextList = ArrayList<String>()
    private var mHandler: Handler? = null

    /**
     * @param textSize  textsize
     * @param padding   padding
     * @param textColor textcolor
     */
    fun setText(textSize: Float, padding: Int, textColor: Int) {
        mTextSize = textSize
        mPadding = padding
        mTextColor = textColor
    }

    fun setAnimTime(animDuration: Long) {
        mAnimateTime = animDuration
        setFactory(this)
    }

    private fun setupAnimation() {
        val inAnim: Animation = TranslateAnimation(0f, 0f, height.toFloat(), 0f)
        inAnim.duration = mAnimateTime
        inAnim.interpolator = LinearInterpolator()
        val outAnim: Animation = TranslateAnimation(0f, 0f, 0f, (-height).toFloat())
        outAnim.duration = mAnimateTime
        outAnim.interpolator = LinearInterpolator()
        inAnimation = inAnim
        outAnimation = outAnim
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h != oldh) {
            setupAnimation()
        }
    }

    /**
     * set time.
     *
     * @param time
     */
    fun setTextStillTime(time: Long) {
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    FLAG_START_AUTO_SCROLL -> {
                        if (mTextList.isNotEmpty()) {
                            mCurrentId++
                            setText(mTextList[mCurrentId % mTextList.size])
                        }
                        this.sendEmptyMessageDelayed(FLAG_START_AUTO_SCROLL, time)
                    }
                    FLAG_STOP_AUTO_SCROLL -> this.removeMessages(FLAG_START_AUTO_SCROLL)
                }
            }
        }
    }

    /**
     * set Data list.
     *
     * @param titles
     */
    fun setTextList(titles: List<String>) {
        mTextList.clear()
        mTextList.addAll(titles)
        mCurrentId = -1
    }

    /**
     * start auto scroll
     */
    fun startAutoScroll() {
        mScrollState = STATE_SCROLL
        mHandler?.sendEmptyMessage(FLAG_START_AUTO_SCROLL)
    }

    /**
     * stop auto scroll
     */
    fun stopAutoScroll() {
        mScrollState = STATE_PAUSE
        mHandler?.sendEmptyMessage(FLAG_STOP_AUTO_SCROLL)
    }

    override fun makeView(): View {
        val tv = TextView(context)
        tv.gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
        tv.maxLines = 1
        tv.setPadding(mPadding, mPadding, mPadding, mPadding)
        tv.setTextColor(mTextColor)
        tv.textSize = mTextSize
        tv.isClickable = true
        tv.setOnClickListener {
            if (mItemClickListener != null && mTextList.size > 0 && mCurrentId != -1) {
                mItemClickListener?.onItemClick(mCurrentId % mTextList.size)
            }
        }
        return tv
    }

    /**
     * set onclick listener
     *
     * @param itemClickListener listener
     */
    fun setOnItemClickListener(itemClickListener: OnItemClickListener?) {
        this.mItemClickListener = itemClickListener
    }

    /**
     * item click listener
     */
    interface OnItemClickListener {
        /**
         * callback
         *
         * @param position position
         */
        fun onItemClick(position: Int)
    }

    val isScroll: Boolean
        get() = mScrollState == STATE_SCROLL

    val isPause: Boolean
        get() = mScrollState == STATE_PAUSE

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler?.removeCallbacksAndMessages(null)
    }
}