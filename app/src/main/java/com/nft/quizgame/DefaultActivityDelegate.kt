package com.nft.quizgame

import android.view.Window
import com.nft.quizgame.common.BaseActivity
import com.nft.quizgame.common.BaseActivityDelegate
import com.nft.quizgame.sound.SoundManager

class DefaultActivityDelegate(activity: BaseActivity) : BaseActivityDelegate(activity) {

    override fun onCreate() {
        activity.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        activity.setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        SoundManager.onResume()
    }

    override fun onPause() {
        SoundManager.onPause()
    }

    override fun onStop() {
    }

    override fun onStart() {
    }

    override fun onDestroy() {
    }
}