package com.nft.quizgame.dialog

import android.view.animation.Animation

class QuizAnimationDialogHelper {

    private val animationList = arrayListOf<Animation>()

    fun addAnimation(anim: Animation) {
        animationList.add(anim)
    }

    fun clearAllAnimation() {
        for (anim in animationList) {
            anim.cancel()
        }
    }

}