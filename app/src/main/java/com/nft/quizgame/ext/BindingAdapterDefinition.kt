package com.nft.quizgame.ext

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.nft.quizgame.R
import com.nft.quizgame.common.view.CircleProgressBar
import com.nft.quizgame.common.view.ShadowLayout
import com.nft.quizgame.function.quiz.view.OptionState

@BindingAdapter(value = ["optionImgState"], requireAll = false)
fun setupOptionImage(view: ImageView, optionState: Int) {
    when (optionState) {
        OptionState.STATE_NORMAL -> view.visibility = View.INVISIBLE
        OptionState.STATE_PRESSED_CORRECT -> {
            view.visibility = View.VISIBLE
            view.setImageResource(R.mipmap.icon_tick_white)
        }
        OptionState.STATE_PRESSED_INCORRECT -> {
            view.visibility = View.VISIBLE
            view.setImageResource(R.mipmap.icon_wrong_white)
        }
        OptionState.STATE_TIPS_CORRECT -> {
            view.visibility = View.VISIBLE
            view.setImageResource(R.mipmap.icon_tick_green)
        }
        OptionState.STATE_TIPS_INCORRECT -> {
            view.visibility = View.VISIBLE
            view.setImageResource(R.mipmap.icon_wrong_red)
        }
    }
}

@BindingAdapter(value = ["optionBgState"], requireAll = false)
fun setupOptionBg(view: ViewGroup, optionState: Int) {
    when (optionState) {
        OptionState.STATE_NORMAL -> view.setBackgroundResource(R.drawable.quiz_option_bg_normal)
        OptionState.STATE_PRESSED_CORRECT -> view.setBackgroundResource(R.drawable.quiz_option_bg_pressed_green)
        OptionState.STATE_PRESSED_INCORRECT -> view.setBackgroundResource(R.drawable.quiz_option_bg_pressed_red)
        OptionState.STATE_TIPS_CORRECT -> view.setBackgroundResource(R.drawable.quiz_option_bg_outline_green)
        OptionState.STATE_TIPS_INCORRECT -> view.setBackgroundResource(R.drawable.quiz_option_bg_outline_red)
    }
}

@BindingAdapter(value = ["optionTextColor"], requireAll = false)
fun setupOptionTextColor(textView: TextView, optionState: Int) {
    when (optionState) {
        OptionState.STATE_NORMAL -> {
            textView.run {
                setTextColor(Color.parseColor("#787878"))
                setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
            }
        }
        OptionState.STATE_PRESSED_CORRECT -> {
            textView.run {
                setTextColor(Color.parseColor("#FFFFFF"))
                setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
            }
        }
        OptionState.STATE_PRESSED_INCORRECT -> {
            textView.run {
                setTextColor(Color.parseColor("#FFFFFF"))
                setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
            }
        }
        OptionState.STATE_TIPS_CORRECT -> {
            textView.run {
                setTextColor(Color.parseColor("#2CDD9B"))
                setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
            }
        }
        OptionState.STATE_TIPS_INCORRECT -> {
            textView.run {
                setTextColor(Color.parseColor("#787878"))
                setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
            }
        }
    }
}

@BindingAdapter(value = ["optionShadowState"], requireAll = false)
fun setupOptionShadow(view: ShadowLayout, optionState: Int) {
    when (optionState) {
        OptionState.STATE_NORMAL -> view.shadowColor = Color.parseColor("#C1D8FF")
        OptionState.STATE_PRESSED_CORRECT, OptionState.STATE_TIPS_CORRECT -> view.shadowColor =
                Color.parseColor("#E4FFE2")
        OptionState.STATE_PRESSED_INCORRECT, OptionState.STATE_TIPS_INCORRECT -> view.shadowColor =
                Color.parseColor("#FFC3C1")
    }
}

@BindingAdapter(value = ["circleProgress"], requireAll = false)
fun setupCircleProgress(view: CircleProgressBar, progress: Int) {
    view.setProgress(progress)
}

@BindingAdapter(value = ["cardId"], requireAll = false)
fun setupCardImage(view: ImageView, cardId: Int) {
    view.setImageResource(cardId)
}

@BindingAdapter(value = ["isChallengeDone"], requireAll = false)
fun setupChallengeState(view: ImageView, isChallengeDone: Boolean) {
    if (isChallengeDone) {
        view.setImageResource(R.mipmap.icon_challenge_finished)
    } else {
        view.setImageResource(R.mipmap.icon_challenge_unfinished)
    }
}

@BindingAdapter(value = ["isChallengeDone"], requireAll = false)
fun setupChallengeState(view: TextView, isChallengeDone: Boolean) {
    if (isChallengeDone) {
        view.setText(R.string.challenge_done)
        view.setTextColor(Color.parseColor("#0387FF"))
    } else {
        view.setText(R.string.challenge_undone)
        view.setTextColor(Color.parseColor("#A5A5A5"))
    }
}