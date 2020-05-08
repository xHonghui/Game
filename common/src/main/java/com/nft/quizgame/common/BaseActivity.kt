package com.nft.quizgame.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    lateinit var activityDelegate: BaseActivityDelegate

    abstract fun createDelegate():BaseActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityDelegate = createDelegate()
        activityDelegate.onCreate()
    }

    override fun onResume() {
        super.onResume()
        activityDelegate.onResume()
    }

    override fun onStart() {
        super.onStart()
        activityDelegate.onStart()
    }

    override fun onPause() {
        super.onPause()
        activityDelegate.onPause()
    }

    override fun onStop() {
        super.onStop()
        activityDelegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityDelegate.onDestroy()
    }

    override fun onBackPressed() {
        if (!activityDelegate.dispatchBackPressedEvent(activityDelegate.currentFragment)) {
            super.onBackPressed()
        }
    }
}