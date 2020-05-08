package com.nft.quizgame.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import androidx.lifecycle.Observer
import com.nft.quizgame.R
import com.nft.quizgame.ext.getStyleSpanString
import com.nft.quizgame.ext.post
import com.nft.quizgame.view.CoinAnimationLayer

class ChallengeSuccessDialog(activity: Activity, adModuleId: Int, private val bonus: Int, coinAnimEndLoc: IntArray,
                             coinAnimObserver: Observer<Float>) : QuizSimpleDialog(activity, adModuleId) {

    init {
        coinAnimationHelper = CoinAnimationDialogHelper(this, coinAnimEndLoc, coinAnimObserver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logo(R.mipmap.dialog_logo_challenge_success)
        logoBg(R.mipmap.dialog_logo_light_bg, true)

        val title = activity.getString(R.string.challenge_success_title2, bonus)
        val spanTitle = title.getStyleSpanString(bonus.toString(),
                color = Color.parseColor("#35A2FF"),
                style = Typeface.BOLD,size = mNumberTextSize,
                flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        title(text = spanTitle)
        desc(R.string.challenge_success_desc)
        post {
            coinAnimationHelper?.startCoinAnimation(bonus, CoinAnimationLayer.DEFAULT_COIN_ANIMATION_COUNT * 2)
        }
    }
}