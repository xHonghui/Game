package com.nft.quizgame.function.quiz.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.nft.quizgame.BuildConfig
import com.nft.quizgame.R
import com.nft.quizgame.common.utils.DrawUtils
import com.nft.quizgame.databinding.QuizContentOptionItemViewBinding
import com.nft.quizgame.function.quiz.BaseQuizFragment

class QuizOptionGroup : LinearLayout {
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, style: Int) : super(context, attrs, style)

    val optionStateList = arrayListOf<OptionState>()

    init {
        orientation = VERTICAL
    }

    fun setOptions(delegate: BaseQuizFragment.BaseActionDelegate<*>, options: List<String>, correctAnswer: Int) {
        optionStateList.clear()
        optionStateList.addAll(options.mapIndexed { index, title -> OptionState(index, title, OptionState.STATE_NORMAL) })

        val pendingViews = arrayListOf<View>()
        for (i in optionStateList.indices) {
            var child: View? = getChildAt(i)
            if (child == null) {
                val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                if (i > 0) {
                    lp.topMargin = DrawUtils.dip2px(10f)
                }
                child = LayoutInflater.from(context).inflate(R.layout.quiz_content_option_item_view, null)
                child!!.layoutParams = lp
                pendingViews.add(child)
            }
            DataBindingUtil.bind<QuizContentOptionItemViewBinding>(child)?.apply {
                this.delegate = delegate
                this.optionState = optionStateList[i]
                if (correctAnswer == optionStateList[i].answer) {
                    this.imgDummy.setBackgroundColor(Color.GREEN)
                } else {
                    this.imgDummy.setBackgroundColor(Color.RED)
                }
                this.imgDummy.visibility = if (BuildConfig.DEBUG) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }

        }
        pendingViews.forEach { addView(it) }

        if (childCount > options.size) {
            val startIndex = options.size
            for (i in startIndex until childCount) {
                removeViewAt(i)
            }
        }
    }
}